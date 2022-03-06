package com.ilpet.yabm.activities;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
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
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.AlarmReceiver;
import com.ilpet.yabm.utils.Connection;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.LoadingDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class InsertBookmarkActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private static final int DATE_ERROR = -1;
    private static final int TIME_ERROR = -2;
    private EditText link, title;
    private TextView reminderTitle, optionalField;
    private String category;
    private DatabaseHandler db;
    private Bookmark bookmark;
    private ArrayList<Category> categories;
    private Spinner spinnerCategories;
    private boolean isPressed = false;
    private int pressedCounter = 0;
    private int year, month, day, hour, minute;
    private long alarmStartTime = -1;
    private boolean setRemainder = false;
    private boolean isEditMode = false;
    private LoadingDialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_bookmark);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.new_bookmark_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        db = DatabaseHandler.getInstance(getApplicationContext());

        spinnerCategories = findViewById(R.id.spinner1);
        spinnerCategories.setOnItemSelectedListener(this);
        link = findViewById(R.id.insert_link);
        title = findViewById(R.id.insert_title);
        reminderTitle = findViewById(R.id.reminder_title);
        optionalField = findViewById(R.id.optional_field);
        ImageButton newCategory = findViewById(R.id.new_category);
        ImageButton addRemainder = findViewById(R.id.add_remainder);
        TextView date = findViewById(R.id.inserted_date);
        ImageButton modifyRemainder = findViewById(R.id.modify_reminder);
        ImageButton removeRemainder = findViewById(R.id.remove_reminder);
        bookmark = new Bookmark();
        categories = db.getCategories();
        ArrayList<String> filteredCategories = new ArrayList<>();
        loadingDialog = new LoadingDialog(InsertBookmarkActivity.this);
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }

        for (Category category : categories) {
            filteredCategories.add(category.getCategoryTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, filteredCategories);
        spinnerCategories.setAdapter(adapter);

        if (intent.getExtras() != null) {
            if (intent.getSerializableExtra("bookmark") != null) {
                isEditMode = true;
                bookmark = (Bookmark) intent.getSerializableExtra("bookmark");
                String category = intent.getStringExtra("category");
                toolbarTitle.setText(R.string.modify_bookmark);
                if (bookmark.getReminder() != -1) {
                    addRemainder.setVisibility(View.INVISIBLE);
                    optionalField.setVisibility(View.INVISIBLE);
                    date.setVisibility(View.VISIBLE);
                    modifyRemainder.setVisibility(View.VISIBLE);
                    removeRemainder.setVisibility(View.VISIBLE);
                    reminderTitle.setText(getString(R.string.reminder_inserted));
                    date.setText(DateFormat.format("dd/MM/yyyy HH:mm", bookmark.getReminder()));
                } else {
                    addRemainder.setVisibility(View.VISIBLE);
                    date.setVisibility(View.INVISIBLE);
                    modifyRemainder.setVisibility(View.INVISIBLE);
                    removeRemainder.setVisibility(View.INVISIBLE);
                }
                link.setText(bookmark.getLink());
                title.setText(bookmark.getTitle());
                setSpinnerItem(category);
            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            if (isEditMode) {
                if (bookmark.getTitle() == null) {
                    if (!bookmark.getLink().equals(link.getText().toString()) ||
                            !bookmark.getCategory().equals(db.getCategoryId(category))) {
                        confirmDialog(link.getText().toString(), db.getCategoryId(category));
                    } else {
                        if (!isActivityRunning()) {
                            Intent activityIntent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                            startActivity(activityIntent);
                        }
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                } else {
                    if (!bookmark.getLink().equals(link.getText().toString()) ||
                            !bookmark.getTitle().equals(title.getText().toString()) ||
                            !bookmark.getCategory().equals(db.getCategoryId(category))) {
                        confirmDialog(link.getText().toString(), db.getCategoryId(category));
                    } else {
                        if (!isActivityRunning()) {
                            Intent activityIntent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                            startActivity(activityIntent);
                        }
                        finish();
                        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                    }
                }

            } else {
                if (link.getText().toString().isEmpty()) {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                } else {
                    exitConfirmDialog();
                }
            }
        });

        FloatingActionButton fab = findViewById(R.id.insert_link_button);
        fab.setOnClickListener(view -> {
            String link = this.link.getText().toString();
            if (link.isEmpty()) {
                Toast.makeText(getApplicationContext(),
                        "Inserisci un link!", Toast.LENGTH_LONG)
                        .show();
            } else {
                confirmDialog(link, db.getCategoryId(category));
            }
        });

        link.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Patterns.WEB_URL.matcher(link.getText().toString()).matches()) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                    link.setError(getString(R.string.invalid_link));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        title.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (title.getText().toString().length() >= 30) {
                    title.setError(getString(R.string.max_title));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        newCategory.setOnClickListener(view -> {
            LayoutInflater layoutInflater = LayoutInflater.from(InsertBookmarkActivity.this);
            View dialogView = layoutInflater.inflate(R.layout.new_category_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(InsertBookmarkActivity.this)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton("Annulla", null)
                    .create();

            final EditText input = dialogView.findViewById(R.id.user_input);
            TextView title = dialogView.findViewById(R.id.title);
            title.setText(R.string.new_category_title);
            input.setHint("Inserisci la categoria");

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    if (!input.getText().toString().isEmpty()) {
                        Category category = new Category();
                        category.setCategoryTitle(input.getText().toString());
                        String result = db.addCategory(category);
                        if (result != null) {
                            categories.add(category);
                            filteredCategories.add(category.getCategoryTitle());
                            dialog.dismiss();
                            Toast.makeText(InsertBookmarkActivity.this,
                                    "Categoria inserita correttamente", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(InsertBookmarkActivity.this,
                                    "Categoria già esistente!", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(InsertBookmarkActivity.this,
                                "Inserisci il nome di una categoria!", Toast.LENGTH_LONG).show();
                    }
                });

            });
            dialog.show();
        });

        addRemainder.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            final AlertDialog.Builder alert = new AlertDialog.Builder(InsertBookmarkActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker, null);
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
                cancelButton.setText(R.string.cancel);
                if (isPressed) {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.INVISIBLE);
                    confirmButton.setText(R.string.next);
                    isPressed = false;
                    pressedCounter = 0;
                } else {
                    alertDialog.dismiss();
                }
            });

            confirmButton.setOnClickListener(v -> {
                confirmButton.setText(R.string.confirm);
                cancelButton.setText(R.string.retry);
                datePicker.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                isPressed = true;
                pressedCounter++;
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

                        } else if (result == TIME_ERROR) {
                            Toast.makeText(getApplicationContext(),
                                    "L'orario non è valido", Toast.LENGTH_LONG)
                                    .show();
                        } else {
                            date.setText(DateFormat.format("dd/MM/yyyy HH:mm", alarmStartTime));

                            addRemainder.setVisibility(View.INVISIBLE);
                            date.setVisibility(View.VISIBLE);
                            modifyRemainder.setVisibility(View.VISIBLE);
                            removeRemainder.setVisibility(View.VISIBLE);
                            optionalField.setVisibility(View.INVISIBLE);

                            reminderTitle.setText(R.string.reminder_inserted);
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
            final AlertDialog.Builder alert = new AlertDialog.Builder(InsertBookmarkActivity.this);
            View dialogView = getLayoutInflater().inflate(R.layout.date_time_picker, null);
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
                cancelButton.setText(R.string.cancel);
                if (isPressed) {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.INVISIBLE);
                    confirmButton.setText(R.string.next);
                    isPressed = false;
                    pressedCounter = 0;
                } else {
                    alertDialog.dismiss();
                }
            });

            confirmButton.setOnClickListener(v -> {
                confirmButton.setText(R.string.confirm);
                cancelButton.setText(R.string.cancel);
                datePicker.setVisibility(View.INVISIBLE);
                timePicker.setVisibility(View.VISIBLE);
                isPressed = true;
                pressedCounter++;
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

                        } else if (result == TIME_ERROR) {
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
            optionalField.setVisibility(View.VISIBLE);
            reminderTitle.setText(R.string.new_reminder);
            Toast.makeText(getApplicationContext(),
                    "Promemoria eliminato", Toast.LENGTH_LONG)
                    .show();
            setRemainder = false;
        });
    }

    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            link.setText(sharedText);
        }
    }

    public void setSpinnerItem(String category) {
        for (int i = 0; i < spinnerCategories.getCount(); i++) {
            if (spinnerCategories.getItemAtPosition(i).equals(category)) {
                spinnerCategories.setSelection(i);
                return;
            }
        }
    }

    private void confirmDialog(String link, String categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Handler handler = new Handler();
        if (isEditMode) {
            builder.setMessage("Sei sicuro di voler modificare il segnalibro?")
                    .setCancelable(false)
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Sì", (dialogInterface, i) -> {
                        loadingDialog.startLoading();
                        getBookmarkInformation(link, categoryId);
                        handler.postDelayed(() -> loadingDialog.dismissLoading(), 400);
                    });
        } else {
            builder.setMessage("Sei sicuro di voler inserire il segnalibro?")
                    .setCancelable(false)
                    .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                    .setPositiveButton("Sì", (dialogInterface, i) -> {
                        loadingDialog.startLoading();
                        getBookmarkInformation(link, categoryId);
                        handler.postDelayed(() -> loadingDialog.dismissLoading(), 400);
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
    public void onNothingSelected(AdapterView<?> parent) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        search.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InsertBookmarkActivity.this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        return true;
    }

    private void getBookmarkInformation(String link, String categoryId) {
        if (isEditMode) {
            bookmark.setImage(null);
        }

        Data data = new Data.Builder()
                .putString("link", String.valueOf(link))
                .build();

        WorkRequest workRequest =
                new OneTimeWorkRequest.Builder(Connection.class)
                        .setInputData(data)
                        .build();

        WorkManager.getInstance(this).enqueue(workRequest);

        WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                .observe(this, info -> {
                    if (info != null && info.getState().isFinished()) {
                        bookmark.setLink(link);
                        bookmark.setReminder(alarmStartTime);
                        bookmark.setCategory(categoryId);
                        String titleText = info.getOutputData().getString("title");

                        if (isEditMode) {
                            if (!title.getText().toString().isEmpty()) {
                                if (!bookmark.getTitle().equals(title.getText().toString())) {
                                    bookmark.setTitle(title.getText().toString());
                                }
                            }
                        } else {
                            if (title.getText().toString().isEmpty()) {
                                bookmark.setTitle(titleText);
                            } else {
                                bookmark.setTitle(title.getText().toString());
                            }
                        }
                        bookmark.setImage(info.getOutputData().getString("image"));
                        bookmark.setDescription(info.getOutputData().getString("description"));
                        String itemType = info.getOutputData().getString("itemType");
                        if (itemType == null) {
                            bookmark.setType(Bookmark.ItemType.SIMPLE);
                        } else {

                            bookmark.setType(Bookmark.ItemType.valueOf(itemType));
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        Date date = new Date();
                        bookmark.setDate(dateFormat.format(date));
                        insertBookmark();
                    }
                });
    }

    private void setReminder() {
        Intent intent = new Intent(InsertBookmarkActivity.this, AlarmReceiver.class);
        int notificationId = (int) SystemClock.uptimeMillis();
        Bundle args = new Bundle();
        args.putSerializable("bookmark", bookmark);
        intent.putExtra("data", args);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("category", category);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                InsertBookmarkActivity.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            alarm.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
        } else {
            alarm.setExact(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);
        }
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
        if (isEditMode) {
            queryResult = db.updateBookmark(bookmark);
            if (queryResult) {
                if (setRemainder) {
                    setReminder();
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro modificato!", Toast.LENGTH_LONG)
                        .show();
                intent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
                    setReminder();
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro aggiunto!", Toast.LENGTH_LONG)
                        .show();
                if (!isActivityRunning()) {
                    Intent activityIntent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                    startActivity(activityIntent);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Segnalibro già presente!", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    private void exitConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler uscire?\nTutti i cambiamenti andranno persi")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    if (!isActivityRunning()) {
                        Intent intent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (isEditMode) {
            if (!bookmark.getLink().equals(link.getText().toString()) ||
                    !bookmark.getTitle().equals(title.getText().toString()) ||
                    !bookmark.getCategory().equals(db.getCategoryId(category))) {
                confirmDialog(link.getText().toString(), db.getCategoryId(category));
            } else {
                if (!isActivityRunning()) {
                    Intent activityIntent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                    startActivity(activityIntent);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        } else {
            if (link.getText().toString().isEmpty()) {
                if (!isActivityRunning()) {
                    Intent activityIntent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                    startActivity(activityIntent);
                }
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                exitConfirmDialog();
            }
        }
    }

    private boolean isActivityRunning() {
        ArrayList<String> runningActivities = new ArrayList<>();

        ActivityManager activityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);

        ArrayList<ActivityManager.RunningTaskInfo> services = (ArrayList<ActivityManager.RunningTaskInfo>) activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (ActivityManager.RunningTaskInfo service : services) {
            runningActivities.add(service.toString());
        }
        return runningActivities.contains("ComponentInfo{com.app/com.app.main.MyActivity}");
    }
}