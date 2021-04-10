package com.example.linkcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class Settings extends AppCompatActivity {
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private RelativeLayout themeSetting;
    private RelativeLayout categoriesSetting;
    private RelativeLayout startList;
    private RelativeLayout sendFeedback;
    private TextView appVersion;
    private DatabaseHandler db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Impostazioni");
        setSupportActionBar(toolbar);

        db = DatabaseHandler.getInstance(getApplicationContext());

        themeSetting = findViewById(R.id.theme_setting);
        categoriesSetting = findViewById(R.id.categories_setting);
        startList = findViewById(R.id.list_setting);
        sendFeedback = findViewById(R.id.feedback_setting);
        appVersion = findViewById(R.id.version);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });

        setApplicationInfo();
        themeClickListener();
        categoriesClickListener();
        starListClickListener();
        feedbackClickListener();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem search = menu.findItem(R.id.search);
        MenuItem filter = menu.findItem(R.id.filter);
        MenuItem settings = menu.findItem(R.id.settings);
        search.setVisible(false);
        filter.setVisible(false);
        settings.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void themeClickListener() {
        themeSetting.setOnClickListener(v -> {
            int checkedItem = 0;

            String[] themes = { "Default", "Chiaro", "Scuro" };
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setSingleChoiceItems(themes, checkedItem, (dialog, which) -> {
                // TODO THEME IMPLEMENTATION
                dialog.dismiss();
            });
            builder.show();
        });
    }

    private void categoriesClickListener() {
        categoriesSetting.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, Categories.class);
            startActivity(intent);
        });
    }

    private void starListClickListener() {
        startList.setOnClickListener(v -> {
            int checkedItem = 0;

            ArrayList<String> list = new ArrayList<>();
            list.add("Tutti i segnalibri");
            list.addAll(db.getAllCategories());

            String[] categories = list.toArray(new String[0]);

            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setSingleChoiceItems(categories, checkedItem, (dialog, which) -> {
                // TODO THEME IMPLEMENTATION
                dialog.dismiss();
            });
            builder.show();
        });
    }

    private void setApplicationInfo() {
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().
                    getPackageInfo(getApplicationContext().getPackageName(), 0);
            appVersion.setText(pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void feedbackClickListener() {
        categoriesSetting.setOnClickListener(v -> {
            // TODO FEEDBACK IMPLEMENTATION
        });
    }

}