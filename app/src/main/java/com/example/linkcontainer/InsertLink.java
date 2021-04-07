package com.example.linkcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

import java.util.ArrayList;
import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class InsertLink extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private EditText inputLink;
    private Intent intent;
    private String category;
    private DatabaseHandler db;
    private Bookmark bookmark;
    private ArrayList<String> categories;
    private boolean isPressed = false;
    private int pressedCounter = 0;
    private int notificationId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_link);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Nuovo segnalibro");
        db = DatabaseHandler.getInstance(getApplicationContext());

        Spinner dropdown = findViewById(R.id.spinner1);
        dropdown.setOnItemSelectedListener(this);
        inputLink = findViewById(R.id.insert_link);
        ImageButton newCategory = findViewById(R.id.new_category);
        bookmark = new Bookmark();
        ImageButton add_remainder = findViewById(R.id.add_remainder);

        Intent intent = getIntent();
        if(intent.getExtras() != null){
            inputLink.setText(intent.getStringExtra("url"));
        }

        categories = db.getCategories();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        dropdown.setAdapter(adapter);

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
                    } else {
                        Toast.makeText(InsertLink.this,
                                "Categoria già esistente!", Toast.LENGTH_LONG).show();
                    }
                });
                alertbox.setNegativeButton("Annulla", (arg0, arg1) -> { });
                alertbox.show();
            }
        });

        add_remainder.setOnClickListener(new View.OnClickListener() {
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
                        if (pressedCounter == 2) {
                            pressedCounter ++;
                            Intent intent = new Intent(InsertLink.this, AlarmReceiver.class);
                            intent.putExtra("notificationId", notificationId);
                            intent.putExtra("todo", "TODO");

                            PendingIntent alarmIntent = PendingIntent.getBroadcast(
                                    InsertLink.this, 0, intent,
                                    PendingIntent.FLAG_CANCEL_CURRENT);

                            AlarmManager alarm = (AlarmManager) getSystemService(ALARM_SERVICE);

                            int hour = timePicker.getCurrentHour();
                            int minute = timePicker.getCurrentMinute();

                            int day = datePicker.getDayOfMonth();
                            int month = datePicker.getMonth();
                            int year = datePicker.getYear();

                            Calendar startTime = Calendar.getInstance();
                            startTime.set(Calendar.HOUR_OF_DAY, hour);
                            startTime.set(Calendar.MINUTE, minute);
                            startTime.set(Calendar.SECOND, 0);
                            startTime.set(Calendar.DAY_OF_MONTH, day);
                            startTime.set(Calendar.MONTH, month);
                            startTime.set(Calendar.YEAR, year);

                            long alarmStartTime = startTime.getTimeInMillis();
                            alarm.set(AlarmManager.RTC_WAKEUP, alarmStartTime, alarmIntent);

                            Toast.makeText(getApplicationContext(),
                                    "Promemoria impostato correttamente", Toast.LENGTH_LONG)
                                    .show();
                        }

                    }
                });
                alertDialog.show();
            }
        });
    }

    private void confirmDialog(String link, String categoryId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler inserire il link?")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    getUrlInformations(link, categoryId);
                });
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
            Intent intent = new Intent(InsertLink.this, Categories.class);
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
                        boolean queryResult = db.addBookmark(bookmark);

                        Log.i("IAHD", String.valueOf(queryResult));

                        if (queryResult) {
                            intent = new Intent(InsertLink.this, MainActivity.class);
                            loadingDialog.dismissLoading();
                            startActivity(intent);
                            finish();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Segnalibro già presente!", Toast.LENGTH_LONG)
                                .show();
                    }

                    } else {
                        Toast.makeText(getApplicationContext(),
                                "Errore!", Toast.LENGTH_LONG)
                                .show();
                    }
                        },
                        error -> {
                            Toast.makeText(getApplicationContext(),
                                    error.getMessage(), Toast.LENGTH_LONG)
                                    .show();
                        });
    }
}