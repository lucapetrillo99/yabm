package com.example.linkcontainer;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {
    private static final String THEME = "theme";
    private static final String CATEGORY = "category";
    private RelativeLayout themeSetting;
    private RelativeLayout categoriesSetting;
    private RelativeLayout startCategory;
    private RelativeLayout importExport;
    private RelativeLayout backup;
    private RelativeLayout sendFeedback;
    private RelativeLayout appInfo;
    private DatabaseHandler db;

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
        appInfo = findViewById(R.id.information_setting);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(v -> finish());

        appInfoClickListener();
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
        MenuItem settings = menu.findItem(R.id.settings);
        search.setVisible(false);
        settings.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    private void themeClickListener() {
        themeSetting.setOnClickListener(v -> {
            SettingsManager themeManager = new SettingsManager(getApplicationContext(), THEME);
            int checkedItem = themeManager.getTheme();

            String[] themes = { "Default", "Chiaro", "Scuro" };
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
            Intent intent = new Intent(SettingsActivity.this, Categories.class);
            startActivity(intent);
        });
    }

    private void starCategoryClickListener() {
        startCategory.setOnClickListener(v -> {
            SettingsManager categoryManager = new SettingsManager(getApplicationContext(), CATEGORY);
            int checkedItem = 0;
            String startCategory = categoryManager.getCategory();
            ArrayList<Category> categoryList = new ArrayList<>(db.getAllCategories());
            ArrayList<String> list = new ArrayList<>();
            list.add("Tutti i segnalibri");
            for (Category category: categoryList) {
                list.add(category.getCategoryTitle());
            }

            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(startCategory)) {
                    checkedItem = i;
                }
            }

            String[] categories = list.toArray(new String[0]);
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
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
            Intent intent = new Intent(SettingsActivity.this, BookmarksManagerActivity.class);
            startActivity(intent);
        });
    }

    private void backupClickListener() {
        backup.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, BackupActivity.class);
            startActivity(intent);
        });
    }

    private void feedbackClickListener() {
        sendFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
            startActivity(intent);
        });
    }
    private void appInfoClickListener() {
        appInfo.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, InfoAppActivity.class);
            startActivity(intent);
        });
    }
}