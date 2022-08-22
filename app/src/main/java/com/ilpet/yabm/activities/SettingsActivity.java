package com.ilpet.yabm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.PasswordManagerDialog;
import com.ilpet.yabm.utils.SettingsManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SettingsActivity extends AppCompatActivity {
    private static final String THEME = "theme";
    private static final String CATEGORY = "category";
    private RelativeLayout themeSetting;
    private RelativeLayout categoriesSetting;
    private RelativeLayout startCategory;
    private RelativeLayout importExport;
    private RelativeLayout backup;
    private RelativeLayout handlePassword;
    private RelativeLayout sendFeedback;
    private RelativeLayout helpSetting;
    private RelativeLayout appInfo;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.settings_title);
        setSupportActionBar(toolbar);

        db = DatabaseHandler.getInstance(getApplicationContext());

        themeSetting = findViewById(R.id.theme_setting);
        categoriesSetting = findViewById(R.id.categories_setting);
        startCategory = findViewById(R.id.start_category_setting);
        importExport = findViewById(R.id.import_export_setting);
        backup = findViewById(R.id.backup_setting);
        handlePassword = findViewById(R.id.password_setting);
        sendFeedback = findViewById(R.id.feedback_setting);
        helpSetting = findViewById(R.id.help_setting);
        appInfo = findViewById(R.id.information_setting);

        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        appInfoClickListener();
        themeClickListener();
        categoriesClickListener();
        starCategoryClickListener();
        importExportClickListener();
        backupClickListener();
        handlePasswordClickListener();
        helpClickListener();
        feedbackClickListener();

    }

    private void helpClickListener() {
        helpSetting.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, HelpMenuActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
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

            String[] themes = {"Default", "Chiaro", "Scuro"};
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
            Intent intent = new Intent(SettingsActivity.this, CategoriesActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void starCategoryClickListener() {
        startCategory.setOnClickListener(v -> {
            SettingsManager categoryManager = new SettingsManager(getApplicationContext(), CATEGORY);
            int checkedItem = 0;
            String startCategory = categoryManager.getCategory();
            ArrayList<Category> categories = new ArrayList<>(db.getAllCategories(null,
                    null));
            List<String> categoriesToString = categories.stream()
                    .map(Category::getCategoryTitle)
                    .collect(Collectors.toList());
            categoriesToString.add(0, getString(R.string.all_bookmarks_title));
            

            for (int i = 0; i < categoriesToString.size(); i++) {
                if (categoriesToString.get(i).equals(startCategory)) {
                    checkedItem = i;
                }
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setSingleChoiceItems(categoriesToString.toArray(new String[0]), checkedItem,
                    (dialog, choice) -> {
                if (!categoryManager.getCategory().equals(categoriesToString.get(choice))) {
                    categoryManager.setCategory(categoriesToString.get(choice));
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
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void backupClickListener() {
        backup.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, BackupActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void handlePasswordClickListener() {
        handlePassword.setOnClickListener(v -> {
            PasswordManagerDialog passwordManagerDialog = new
                    PasswordManagerDialog(this,
                    result -> { });
            passwordManagerDialog.show(getSupportFragmentManager(),
                    "Password Manager dialog");
        });
    }

    private void feedbackClickListener() {
        sendFeedback.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, FeedbackActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    private void appInfoClickListener() {
        appInfo.setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, InfoAppActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
