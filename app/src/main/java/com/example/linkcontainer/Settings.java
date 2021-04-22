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
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.ArrayList;

public class Settings extends AppCompatActivity {
    private static final String THEME = "theme";
    private static final String CATEGORY = "category";
    private RelativeLayout themeSetting;
    private RelativeLayout categoriesSetting;
    private RelativeLayout startCategory;
    private RelativeLayout importExport;
    private RelativeLayout backup;
    private RelativeLayout sendFeedback;
    private TextView appVersion;
    private DatabaseHandler db;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Impostazioni");
        setSupportActionBar(toolbar);

        db = DatabaseHandler.getInstance(getApplicationContext());

        themeSetting = findViewById(R.id.theme_setting);
        categoriesSetting = findViewById(R.id.categories_setting);
        startCategory = findViewById(R.id.start_category_setting);
        importExport = findViewById(R.id.import_export_setting);
        backup = findViewById(R.id.backup_setting);
        sendFeedback = findViewById(R.id.feedback_setting);
        appVersion = findViewById(R.id.version);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(v -> finish());

        setApplicationInfo();
        themeClickListener();
        categoriesClickListener();
        starCategoryClickListener();
        importExportClickListener();
        backupClickListener();
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
            SettingsManager themeManager = new SettingsManager(getApplicationContext(), THEME);
            int checkedItem = themeManager.getTheme();

            String[] themes = { "Default", "Chiaro", "Scuro" };
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setSingleChoiceItems(themes, checkedItem, (dialog, choice) -> {
                if (themeManager.getTheme() != choice) {
                    themeManager.setTheme(choice);
                }
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

    private void starCategoryClickListener() {
        startCategory.setOnClickListener(v -> {
            SettingsManager categoryManager = new SettingsManager(getApplicationContext(), CATEGORY);
            int checkedItem = 0;
            String category = categoryManager.getCategory();
            ArrayList<String> list = new ArrayList<>();
            list.add("Tutti i segnalibri");
            list.addAll(db.getAllCategories());

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(category)) {
                    checkedItem = i;
                }
            }

            String[] categories = list.toArray(new String[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(Settings.this);
            builder.setSingleChoiceItems(categories, checkedItem, (dialog, choice) -> {
                if (!categoryManager.getCategory().equals(categories[choice])) {
                    categoryManager.setCategory(categories[choice]);
                }
                dialog.dismiss();
            });
            builder.show();
        });
    }

    private void importExportClickListener() {
        importExport.setOnClickListener(v -> {
            // TODO CREA CLASSE
            Intent intent = new Intent(Settings.this, BookmarksManagerActivity.class);
            startActivity(intent);
        });
    }

    private void backupClickListener() {
        backup.setOnClickListener(v -> {
            // TODO CREA CLASSE
//            Intent intent = new Intent(Settings.this, FeedbackActivity.class);
//            startActivity(intent);
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
        sendFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }

}