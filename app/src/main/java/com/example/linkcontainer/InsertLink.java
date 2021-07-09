package com.example.linkcontainer;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InsertLink extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText inputLink, title;
    private TextView reminderTitle;
    private String category;
    private DatabaseHandler db;
    private Bookmark bookmark;
    private ArrayList<String> categories;
    private Spinner dropdown;
    private boolean isPressed = false;
    private int pressedCounter = 0;
    private int year, month, day, hour, minute;
    private long alarmStartTime = -1;
    private boolean setRemainder = false;
    private static final int DATE_ERROR = -1;
    private static final int TIME_ERROR = -2;
    private boolean isModified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_link);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Nuovo Segnalibro");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        db = DatabaseHandler.getInstance(getApplicationContext());

        dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);
        inputLink = findViewById(R.id.insert_link);
        title = findViewById(R.id.insert_title);
        reminderTitle = findViewById(R.id.reminder_title);
        ImageButton newCategory = findViewById(R.id.new_category);
        ImageButton addRemainder = findViewById(R.id.add_remainder);
        TextView date = findViewById(R.id.inserted_date);
        ImageButton modifyRemainder = findViewById(R.id.modify_reminder);
        ImageButton removeRemainder = findViewById(R.id.remove_reminder);
        bookmark = new Bookmark();
        categories = db.getCategories();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        dropdown.setAdapter(adapter);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            if (intent.getStringExtra("url") != null) {
                inputLink.setText(intent.getStringExtra("url"));
            } else if (intent.getSerializableExtra("bookmark") != null) {
                isModified = true;
                bookmark = (Bookmark) intent.getSerializableExtra("bookmark");
                String category = intent.getStringExtra("category");
                if (bookmark.getReminder() != -1) {
                    addRemainder.setVisibility(View.INVISIBLE);
                    date.setVisibility(View.VISIBLE);
                    modifyRemainder.setVisibility(View.VISIBLE);
                    removeRemainder.setVisibility(View.VISIBLE);

                    setSpinnerItem(category);
                    reminderTitle.setText("Promemoria inserito:");
                    date.setText(DateFormat.format("dd/MM/yyyy HH:mm", bookmark.getReminder()));
                } else {
                    addRemainder.setVisibility(View.VISIBLE);
                    date.setVisibility(View.INVISIBLE);
                    modifyRemainder.setVisibility(View.INVISIBLE);
                    removeRemainder.setVisibility(View.INVISIBLE);
                }
                inputLink.setText(bookmark.getLink());
                title.setText(bookmark.getTitle());
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (inputLink.getText().toString().isEmpty()) {
                finish();
            } else {
                exitConfirmDialog();
            }
        });

        FloatingActionButton fab = findViewById(R.id.insert_link_button);
        fab.setOnClickListener(view -> {
            String link = inputLink.getText().toString();
            if (link.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        "Inserisci un link!", Toast.LENGTH_LONG)
                        .show();
            } else {
                confirmDialog(link, db.getCategoryId(category));
            }
        });

        inputLink.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Patterns.WEB_URL.matcher(inputLink.getText().toString()).matches()) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                    inputLink.setError("Link non valido");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });

        newCategory.setOnClickListener(view -> {
            LayoutInflater layoutInflater = LayoutInflater.from(InsertLink.this);
            View dialogView = layoutInflater.inflate(R.layout.dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(InsertLink.this)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton("Annulla", null)
                    .create();

            final EditText input = dialogView.findViewById(R.id.user_input);
            TextView title = dialogView.findViewById(R.id.title);
            title.setText("Nuova categoria");
            input.setHint("Inserisci la categoria");

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    if (!input.getText().toString().isEmpty()) {
                        boolean result = db.addCategory(input.getText().toString());
                        if (result) {
                            categories.add(input.getText().toString());
                            dialog.dismiss();
                            Toast.makeText(InsertLink.this,
                                    "Categoria inserita correttamente", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(InsertLink.this,
                                    "Categoria già esistente!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(InsertLink.this,
                                "Inserisci il nome di una categoria!", Toast.LENGTH_LONG).show();
                    }
                });

            });
            dialog.show();
        });

        addRemainder.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            final AlertDialog.Builder alert = new AlertDialog.Builder(InsertLink.this);
            View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker,null);
            DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
            TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
            Button cancelButton = dialogView.findViewById(R.id.previous);
            Button confirmButton = dialogView.findViewById(R.id.next);

            datePicker.setMinDate(calendar.getTimeInMillis());
            timePicker.setIs24HourView(true);
            alert.setView(dialogView);

            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            cancelButton.setOnClickListener(v -> {
                cancelButton.setText("Annulla");
                if (isPressed) {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.INVISIBLE);
                    confirmButton.setText("Avanti");
                    isPressed = false;
                    pressedCounter = 0;
                } else {
                    alertDialog.dismiss();
                }
            });

            confirmButton.setOnClickListener(v -> {
                confirmButton.setText("Conferma");
                cancelButton.setText("Indietro");
                datePicker.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                isPressed = true;
                pressedCounter ++;
                if (pressedCounter > 1) {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();

                    day = datePicker.getDayOfMonth();
                    month = datePicker.getMonth();
                    year = datePicker.getYear();

                    try {
                        int result = setCalendar();

                        if (result == DATE_ERROR) {
                            Toast.makeText(getApplicationContext(),
                                    "La data non è valida", Toast.LENGTH_LONG)
                                    .show();

                        } else if (result == TIME_ERROR){
                            Toast.makeText(getApplicationContext(),
                                    "L'orario non è valido", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            date.setText( DateFormat.format("dd/MM/yyyy HH:mm", alarmStartTime));

                            addRemainder.setVisibility(View.INVISIBLE);
                            date.setVisibility(View.VISIBLE);
                            modifyRemainder.setVisibility(View.VISIBLE);
                            removeRemainder.setVisibility(View.VISIBLE);

                            reminderTitle.setText("Promemoria inserito:");
                            date.setText(DateFormat.format("dd/MM/yyyy HH:mm", alarmStartTime));
                            Toast.makeText(getApplicationContext(),
                                    "Promemoria impostato correttamente", Toast.LENGTH_LONG)
                                    .show();
                            setRemainder = true;
                            alertDialog.dismiss();
                        }
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(),
                                "Qualcosa è andato storto!\n Prova più tardi", Toast.LENGTH_LONG)
                                .show();
                        alertDialog.dismiss();
                    }
                }
            });
            alertDialog.show();
        });

        modifyRemainder.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            final AlertDialog.Builder alert = new AlertDialog.Builder(InsertLink.this);
            View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker,null);
            DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
            TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
            Button cancelButton = dialogView.findViewById(R.id.previous);
            Button confirmButton = dialogView.findViewById(R.id.next);

            pressedCounter = 0;

            datePicker.setMinDate(calendar.getTimeInMillis());
            timePicker.setIs24HourView(true);
            alert.setView(dialogView);

            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            cancelButton.setOnClickListener(v -> {
                cancelButton.setText("Annulla");
                if (isPressed) {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.INVISIBLE);
                    confirmButton.setText("Avanti");
                    isPressed = false;
                    pressedCounter = 0;
                } else {
                    alertDialog.dismiss();
                }
            });

            confirmButton.setOnClickListener(v -> {
                confirmButton.setText("Conferma");
                cancelButton.setText("Indietro");
                datePicker.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                isPressed = true;
                pressedCounter ++;
                if (pressedCounter > 1) {
                    hour = timePicker.getCurrentHour();
                    minute = timePicker.getCurrentMinute();
                    day = datePicker.getDayOfMonth();
                    month = datePicker.getMonth();
                    year = datePicker.getYear();

                    try {
                        int result = setCalendar();

                        if (result == DATE_ERROR) {
                            Toast.makeText(getApplicationContext(),
                                    "La data non è valida", Toast.LENGTH_LONG)
                                    .show();

                        } else if (result == TIME_ERROR){
                            Toast.makeText(getApplicationContext(),
                                    "L'orario non è valido", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            date.setText(DateFormat.format("dd/MM/yyyy HH:mm", alarmStartTime));
                            Toast.makeText(getApplicationContext(),
                                    "Promemoria modificato correttamente", Toast.LENGTH_LONG)
                                    .show();
                            setRemainder = true;
                            alertDialog.dismiss();
                        }
                    } catch (ParseException e) {
                        Toast.makeText(getApplicationContext(),
                                "Qualcosa è andato storto!\n Prova più tardi", Toast.LENGTH_LONG)
                                .show();
                        alertDialog.dismiss();
                    }
                }
            });
            alertDialog.show();
        });

        removeRemainder.setOnClickListener(view -> {
            addRemainder.setVisibility(View.VISIBLE);
            date.setVisibility(View.INVISIBLE);
            modifyRemainder.setVisibility(View.INVISIBLE);
            removeRemainder.setVisibility(View.INVISIBLE);
            reminderTitle.setText("Inserisci un promemoria:");
            Toast.makeText(getApplicationContext(),
                    "Promemoria eliminato", Toast.LENGTH_LONG)
                    .show();
            setRemainder = false;
        });
    }

    public void setSpinnerItem(String category) {
        for (int i = 0; i <  dropdown.getCount(); i ++) {
            if(dropdown.getItemAtPosition(i).equals(category)) {
                dropdown.setSelection(i);
                return;
            }
        }
    }

    private void confirmDialog(String link, String categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (isModified) {
            builder.setMessage("Sei sicuro di voler modificare il segnalibro?")
                    .setCancelable(false)
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Sì", (dialogInterface, i) -> getBookmarkInformation(link, categoryId));
        } else {
            builder.setMessage("Sei sicuro di voler inserire il segnalibro?")
                    .setCancelable(false)
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Sì", (dialogInterface, i) -> getBookmarkInformation(link, categoryId));
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        category = adapterView.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) { }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem filter = menu.findItem(R.id.filter);
        search.setVisible(false);
        filter.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            Intent intent = new Intent(InsertLink.this, SettingsActivity.class);
            startActivity(intent);
            finish();
        return true;
    }

    private void getBookmarkInformation(String link, String categoryId) {
        LoadingDialog loadingDialog = new LoadingDialog(InsertLink.this);
        loadingDialog.startLoading();
        bookmark.setLink(link);
        Utils.getJsoupContent(link).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        Elements metaTags = result.getElementsByTag("meta");

                        for (Element element : metaTags) {
                            if (element.attr("property").equals("og:image")) {
                                bookmark.setImage(element.attr("content"));
                            } else if (element.attr("property").equals("og:site_name")) {
                                if (title.getText().toString().isEmpty()) {
                                    bookmark.setTitle(element.attr("content"));
                                }
                            } else if (element.attr("name").equals("description")) {
                                bookmark.setDescription(element.attr("content"));
                            }
                        }
                        bookmark.setCategory(categoryId);
                        bookmark.setReminder(alarmStartTime);

                        if (bookmark.getTitle() == null && title.getText().toString().isEmpty()) {
                            bookmark.setTitle(link.split("//")[1].split("/")[0]);
                        } else if (!title.getText().toString().isEmpty()) {
                            bookmark.setTitle(title.getText().toString());
                        }
                        insertBookmark();
                        loadingDialog.dismissLoading();

                    } else {
                        loadingDialog.dismissLoading();
                        Toast.makeText(getApplicationContext(),
                                "Non è possibile recuperare le informazioni per questo link!", Toast.LENGTH_LONG)
                                .show();
                    }},
                        error -> {
                            Toast.makeText(getApplicationContext(),
                                    "Non è possibile recuperare le informazioni per questo link!", Toast.LENGTH_LONG)
                                    .show();
                            loadingDialog.dismissLoading();
                        });
    }

    private void setReminder(String message, String link) {
        Intent intent = new Intent(InsertLink.this, AlarmReceiver.class);
        final int notificationId = 0;
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("message", message);
        intent.putExtra("link", link);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                InsertLink.this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
    }

    private int setCalendar() throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        Calendar startTime = Calendar.getInstance();
        Date currentDate = dateFormat.parse(dateFormat.format(startTime.getTime()));
        Date currentTime = timeFormat.parse(timeFormat.format(startTime.getTime()));
        startTime.set(Calendar.HOUR_OF_DAY, hour);
        startTime.set(Calendar.MINUTE, minute);
        startTime.set(Calendar.SECOND, 0);
        startTime.set(Calendar.DAY_OF_MONTH, day);
        startTime.set(Calendar.MONTH, month);
        startTime.set(Calendar.YEAR, year);

        Date insertedDate = dateFormat.parse(dateFormat.format(startTime.getTime()));
        Date insertedTime = timeFormat.parse(timeFormat.format(startTime.getTime()));

        alarmStartTime = startTime.getTimeInMillis();

        assert insertedDate != null;
        if (insertedDate.compareTo(currentDate) < 0) {
            return DATE_ERROR;
        } else if (insertedDate.compareTo(currentDate) == 0 &&
                (Objects.requireNonNull(insertedTime).compareTo(currentTime) < 0)) {
            return TIME_ERROR;
        } else {
            return 0;
        }
    }

    private void insertBookmark() {
        boolean queryResult;
        Intent intent;
        if (isModified) {
            queryResult = db.updateBookmark(bookmark);
            if (queryResult) {
                if (setRemainder) {
                    if (bookmark.getTitle() != null) {
                        setReminder(bookmark.getTitle(), bookmark.getLink());
                    } else {
                        setReminder(bookmark.getLink(), bookmark.getLink());
                    }
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro modificato!", Toast.LENGTH_LONG)
                        .show();
                intent = new Intent(InsertLink.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Impossibilie modificare il segnalibro!\nRiprova più tardi",
                        Toast.LENGTH_LONG).show();
            }
        } else {
            queryResult = db.addBookmark(bookmark);
            if (queryResult) {
                if (setRemainder) {
                    if (bookmark.getTitle() != null) {
                        setReminder(bookmark.getTitle(), bookmark.getLink());
                    } else {
                        setReminder(bookmark.getLink(), bookmark.getLink());
                    }
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro aggiunto!", Toast.LENGTH_LONG)
                        .show();
                finish();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Segnalibro già presente!", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void exitConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler uscire?\nTutti i cambiamenti andranno perssi")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> finish());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (inputLink.getText().toString().isEmpty()) {
            finish();
        } else {
            exitConfirmDialog();
        }
    }
}