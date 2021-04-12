package com.example.linkcontainer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InsertLink extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText inputLink;
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

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_link);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Nuovo Segnalibro");
        setSupportActionBar(toolbar);
        db = DatabaseHandler.getInstance(getApplicationContext());

        dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);
        inputLink = findViewById(R.id.insert_link);
        ImageButton newCategory = findViewById(R.id.new_category);
        bookmark = new Bookmark();
        ImageButton addRemainder = findViewById(R.id.add_remainder);
        TextView date = findViewById(R.id.inserted_date);
        ImageButton modifyRemainder = findViewById(R.id.modify_reminder);
        ImageButton removeRemainder = findViewById(R.id.remove_reminder);

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
                    date.setText(DateFormat.format("dd-MM-yyyy hh:mm a", bookmark.reminder));
                } else {
                    addRemainder.setVisibility(View.VISIBLE);
                    date.setVisibility(View.INVISIBLE);
                    modifyRemainder.setVisibility(View.INVISIBLE);
                    removeRemainder.setVisibility(View.INVISIBLE);
                }
                inputLink.setText(bookmark.getLink());
            }
        }

        FloatingActionButton fab = findViewById(R.id.insert_link_button);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String link = inputLink.getText().toString();
                if (link.isEmpty()) {
                    Toast.makeText(getApplicationContext(),
                            "Inserisci un link!", Toast.LENGTH_LONG)
                            .show();
                } else {
                    if (Patterns.WEB_URL.matcher(link).matches()) {
                        confirmDialog(link, db.getCategoryId(category));
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Inserisci un link valido!", Toast.LENGTH_LONG)
                                .show();
                    }
                }
            }
        });

        newCategory.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                LayoutInflater layoutInflater = LayoutInflater.from(InsertLink.this);
                View dialogView = layoutInflater.inflate(R.layout.dialog, null);
                androidx.appcompat.app.AlertDialog.Builder alertbox = new androidx.appcompat.app.AlertDialog.Builder(InsertLink.this);
                alertbox.setView(dialogView);
                final EditText input = dialogView.findViewById(R.id.user_input);
                TextView title = dialogView.findViewById(R.id.title);
                title.setText("Nuova categoria");
                input.setHint("Inserisci la categoria");

                alertbox.setPositiveButton("OK", (arg0, arg1) -> {
                    boolean result = db.addCategory(input.getText().toString());
                    if (result) {
                        categories.add(input.getText().toString());
                        Toast.makeText(InsertLink.this,
                                "Categoria inserita correttamente", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(InsertLink.this,
                                "Categoria già esistente!", Toast.LENGTH_LONG).show();
                    }
                });
                alertbox.setNegativeButton("Annulla", (arg0, arg1) -> { });
                alertbox.show();
            }
        });

        addRemainder.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(InsertLink.this);
                View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker,null);
                DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                Button cancelButton = dialogView.findViewById(R.id.previous);
                Button confirmButton = dialogView.findViewById(R.id.next);

                alert.setView(dialogView);

                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    }
                });

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                    date.setText( DateFormat.format("dd-MM-yyyy hh:mm a", alarmStartTime));

                                    addRemainder.setVisibility(View.INVISIBLE);
                                    date.setVisibility(View.VISIBLE);
                                    modifyRemainder.setVisibility(View.VISIBLE);
                                    removeRemainder.setVisibility(View.VISIBLE);

                                    date.setText(DateFormat.format("dd-MM-yyyy hh:mm a", alarmStartTime));
                                    Toast.makeText(getApplicationContext(),
                                            "Promemoria impostato correttamente", Toast.LENGTH_LONG)
                                            .show();

                                    alertDialog.dismiss();
                                }
                            } catch (ParseException e) {
                                Toast.makeText(getApplicationContext(),
                                        "Qualcosa è andato storto!\n Prova più tardi", Toast.LENGTH_LONG)
                                        .show();
                                alertDialog.dismiss();
                            }
                        }
                    }
                });
                alertDialog.show();
            }
        });

        modifyRemainder.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(InsertLink.this);
                View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker,null);
                DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                TimePicker timePicker = dialogView.findViewById(R.id.time_picker);
                Button cancelButton = dialogView.findViewById(R.id.previous);
                Button confirmButton = dialogView.findViewById(R.id.next);

                pressedCounter = 0;

                datePicker.updateDate(year, month, day);
                timePicker.setHour(hour);
                timePicker.setMinute(minute);

                alert.setView(dialogView);

                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);
                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                    }
                });

                confirmButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                                    date.setText(DateFormat.format("dd-MM-yyyy hh:mm a", alarmStartTime));

                                    Toast.makeText(getApplicationContext(),
                                            "Promemoria modificato correttamente", Toast.LENGTH_LONG)
                                            .show();

                                    alertDialog.dismiss();
                                }
                            } catch (ParseException e) {
                                Toast.makeText(getApplicationContext(),
                                        "Qualcosa è andato storto!\n Prova più tardi", Toast.LENGTH_LONG)
                                        .show();
                                alertDialog.dismiss();
                            }
                        }
                    }
                });
                alertDialog.show();
            }
        });

        removeRemainder.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(View view) {
                addRemainder.setVisibility(View.VISIBLE);
                date.setVisibility(View.INVISIBLE);
                modifyRemainder.setVisibility(View.INVISIBLE);
                removeRemainder.setVisibility(View.INVISIBLE);
                Toast.makeText(getApplicationContext(),
                        "Promemoria eliminato", Toast.LENGTH_LONG)
                        .show();
                setRemainder = false;
            }
        });
    }

    public void setSpinnerItem(String category) {
        Log.i("ibfiud", "Categoria:" + category);
        for (int i = 0; i <  dropdown.getCount(); i ++) {
            Log.i("ibfiud", (String) dropdown.getItemAtPosition(i));
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
                    .setPositiveButton("Sì", (dialogInterface, i) -> {
                        getUrlInformations(link, categoryId);
                    });
        } else {
            builder.setMessage("Sei sicuro di voler inserire il segnalibro?")
                    .setCancelable(false)
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Sì", (dialogInterface, i) -> {
                        getUrlInformations(link, categoryId);
                    });
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
            Intent intent = new Intent(InsertLink.this, Settings.class);
            startActivity(intent);
            finish();
        return true;
    }

    @SuppressLint("ShowToast")
    private void getUrlInformations(String link, String categoryId) {
        LoadingDialog loadingDialog = new LoadingDialog(InsertLink.this);
        loadingDialog.startLoading();
        Utils.getJsoupContent(link).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(result -> {
                    if (result != null) {
                        Elements metaTags = result.getElementsByTag("meta");

                        for (Element element : metaTags) {
                            if (element.attr("property").equals("og:image")) {
                                bookmark.setImage(element.attr("content"));
                            } else if (element.attr("property").equals("og:site_name")) {
                                bookmark.setTitle(element.attr("content"));

                            } else if (element.attr("name").equals("description")) {
                                bookmark.setDescription(element.attr("content"));
                            }
                        }
                        bookmark.setLink(link);
                        bookmark.setCategory(categoryId);
                        bookmark.setReminder(alarmStartTime);

                        insertBookmark();
                        loadingDialog.dismissLoading();

                    } else {
                        loadingDialog.dismissLoading();
                        Toast.makeText(getApplicationContext(),
                                "Errore!", Toast.LENGTH_LONG)
                                .show();
                    }
                        },
                        error -> {
                            loadingDialog.dismissLoading();
                            Toast.makeText(getApplicationContext(),
                                    "QUA!" + error.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        });
    }

    private void setReminder(String message) {
        Intent intent = new Intent(InsertLink.this, AlarmReceiver.class);
        final int notificationId = 0;
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("message", message);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                InsertLink.this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

        alarm.set(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);

    }

    private int setCalendar() throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

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

        if (insertedDate.compareTo(currentDate) < 0) {
            return DATE_ERROR;
        } else if (insertedDate.compareTo(currentDate) == 0 && (insertedTime.compareTo(currentTime) < 0)) {
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
                        setReminder(bookmark.getTitle());
                    } else {
                        setReminder(bookmark.getLink());
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
                        setReminder(bookmark.getTitle());
                    } else {
                        setReminder(bookmark.getLink());
                    }
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro aggiunto!", Toast.LENGTH_LONG)
                        .show();
                intent = new Intent(InsertLink.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(getApplicationContext(),
                        "Segnalibro già presente!", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
}