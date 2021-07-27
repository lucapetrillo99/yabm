package com.example.linkcontainer;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
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
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InsertBookmarkActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int DATE_ERROR = -1;
    private static final int TIME_ERROR = -2;
    private EditText link, title;
    private TextView reminderTitle;
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
    private TextView addImageTitle;
    private ImageButton addImageButton;
    private ImageView categoryImage;
    private Bitmap image;

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
        ImageButton newCategory = findViewById(R.id.new_category);
        ImageButton addRemainder = findViewById(R.id.add_remainder);
        TextView date = findViewById(R.id.inserted_date);
        ImageButton modifyRemainder = findViewById(R.id.modify_reminder);
        ImageButton removeRemainder = findViewById(R.id.remove_reminder);
        bookmark = new Bookmark();
        categories = db.getCategories();
        ArrayList<String> filteredCategories = new ArrayList<>();

        for(Category category: categories) {
            filteredCategories.add(category.getCategoryTitle());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, filteredCategories);
        spinnerCategories.setAdapter(adapter);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            if (intent.getStringExtra("url") != null) {
                link.setText(intent.getStringExtra("url"));
            } else if (intent.getSerializableExtra("bookmark") != null) {
                isEditMode = true;
                bookmark = (Bookmark) intent.getSerializableExtra("bookmark");
                String category = intent.getStringExtra("category");
                if (bookmark.getReminder() != -1) {
                    addRemainder.setVisibility(View.INVISIBLE);
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
                if (!bookmark.getLink().equals(link.getText().toString()) ||
                        !bookmark.getTitle().equals(title.getText().toString()) ||
                        !bookmark.getCategory().equals(db.getCategoryId(category))) {
                    confirmDialog(link.getText().toString(), db.getCategoryId(category));
                } else {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (Patterns.WEB_URL.matcher(link.getText().toString()).matches()) {
                    fab.setEnabled(true);
                } else {
                    fab.setEnabled(false);
                    link.setError("Link non valido");
                }
            }

            @Override
            public void afterTextChanged(Editable s) { }
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
            addImageTitle = dialogView.findViewById(R.id.add_image_title);
            addImageButton = dialogView.findViewById(R.id.add_image_button);
            categoryImage = dialogView.findViewById(R.id.category_image);
            title.setText(R.string.new_category_title);
            input.setHint("Inserisci la categoria");

            addImageButton.setOnClickListener(v1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                } else {
                    getImageFromDevice();
                }
            });

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view1 -> {
                    if (!input.getText().toString().isEmpty()) {
                        Category category = new Category();
                        category.setCategoryTitle(input.getText().toString());
                        category.setCategoryImage(image);
                        boolean result = db.addCategory(category);
                        if (result) {
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
                cancelButton.setText(R.string.cancel);
                if (isPressed) {
                    datePicker.setVisibility(View.VISIBLE);
                    timePicker.setVisibility(View.INVISIBLE);
                    confirmButton.setText(R.string.cancel);
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
            reminderTitle.setText(R.string.new_reminder);
            Toast.makeText(getApplicationContext(),
                    "Promemoria eliminato", Toast.LENGTH_LONG)
                    .show();
            setRemainder = false;
        });
    }

    ActivityResultLauncher<Intent> importImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                addImageTitle.setVisibility(View.GONE);
                addImageButton.setVisibility(View.GONE);
                categoryImage.setVisibility(View.VISIBLE);
                Uri chosenImageUri = result.getData().getData();

                try {
                    image = MediaStore.Images.
                            Media.getBitmap(this.getContentResolver(), chosenImageUri);
                    categoryImage.setImageBitmap(image);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Impossibile caricare l'immagine!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    });

    public void getImageFromDevice() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        importImageLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showWarningMessage();
            }
        }
    }

    public void setSpinnerItem(String category) {
        for (int i = 0; i <  spinnerCategories.getCount(); i ++) {
            if(spinnerCategories.getItemAtPosition(i).equals(category)) {
                spinnerCategories.setSelection(i);
                return;
            }
        }
    }

    private void confirmDialog(String link, String categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (isEditMode) {
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
        search.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(InsertBookmarkActivity.this, SettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
        return true;
    }

    private void getBookmarkInformation(String link, String categoryId) {
        LoadingDialog loadingDialog = new LoadingDialog(InsertBookmarkActivity.this);
        loadingDialog.startLoading();
        bookmark.setLink(link);
        Utils.getUrlContent(link).subscribeOn(Schedulers.newThread())
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
        Intent intent = new Intent(InsertBookmarkActivity.this, AlarmReceiver.class);
        final int notificationId = 0;
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("message", message);
        intent.putExtra("link", link);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(
                InsertBookmarkActivity.this, 0, intent,
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
        if (isEditMode) {
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
                    if (bookmark.getTitle() != null) {
                        setReminder(bookmark.getTitle(), bookmark.getLink());
                    } else {
                        setReminder(bookmark.getLink(), bookmark.getLink());
                    }
                }
                Toast.makeText(getApplicationContext(),
                        "Segnalibro aggiunto!", Toast.LENGTH_LONG)
                        .show();
                intent = new Intent(InsertBookmarkActivity.this, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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

        builder.setMessage("Sei sicuro di voler uscire?\nTutti i cambiamenti andranno persi")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (link.getText().toString().isEmpty()) {
            finish();
        } else {
            exitConfirmDialog();
        }
    }

    private void showWarningMessage() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "",
                Snackbar.LENGTH_LONG);

        View customView = View.inflate(this, R.layout.storage_warning_dialog, null);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);

        customView.findViewById(R.id.go_to_settings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getPackageName(), null);
            intent.setData(uri);
            startActivity(intent);
        });

        customView.findViewById(R.id.warning_close_button).setOnClickListener(v -> snackbar.dismiss());
        snackbarLayout.addView(customView, 0);
        snackbar.show();
    }
}