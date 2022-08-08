package com.ilpet.yabm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.SettingsManager;

import java.util.ArrayList;
import java.util.Objects;

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
            ArrayList<Category> categoryList = new ArrayList<>(db.getAllCategories(null, null));
            ArrayList<String> list = new ArrayList<>();
            list.add(getString(R.string.all_bookmarks_title));
            for (Category category : categoryList) {
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
            String currentPassword = db.getPassword();
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View dialogView = layoutInflater.inflate(R.layout.password_dialog, null);
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(dialogView)
                    .setPositiveButton(android.R.string.ok, null)
                    .setNegativeButton(getString(R.string.cancel), null)
                    .create();
            TextInputLayout userPasswordLayout = dialogView.findViewById(R.id.current_password_layout);
            EditText passwordText = dialogView.findViewById(R.id.password);
            EditText confirmPasswordText = dialogView.findViewById(R.id.password_confirmation);
            EditText userPassword = dialogView.findViewById(R.id.current_password);
            TextView title = dialogView.findViewById(R.id.password_title);
            if (currentPassword != null) {
                title.setText(getString(R.string.change_password));
                userPasswordLayout.setVisibility(View.VISIBLE);
            } else {
                title.setText(getString(R.string.add_password));
                userPasswordLayout.setVisibility(View.GONE);
            }

            dialog.setOnShowListener(dialogInterface -> {
                Button button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(view -> {
                    String password = Objects.requireNonNull(passwordText.getText()).toString();
                    String confirmedPassword = confirmPasswordText.getText().toString();
                    if (currentPassword != null) {
                        String oldPassword = userPassword.getText().toString();
                        if (oldPassword.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {
                            Toast.makeText(this,
                                            getString(R.string.empty_fields), Toast.LENGTH_LONG).
                                    show();
                            passwordText.getText().clear();
                            confirmPasswordText.getText().clear();
                        } else {
                            if (password.equals(confirmedPassword)) {
                                if (!password.equals(oldPassword)) {
                                    if (db.updatePassword(password)) {
                                        Toast.makeText(this, getString(R.string.password_updated),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(this, getString(R.string.
                                                impossible_update_password), Toast.LENGTH_LONG).show();
                                    }
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(this, getString(R.string.same_previous_password),
                                            Toast.LENGTH_LONG).show();
                                    userPassword.getText().clear();
                                    passwordText.getText().clear();
                                    confirmPasswordText.getText().clear();
                                }
                            } else {
                                Toast.makeText(this,
                                        getString(R.string.passwords_not_match),
                                        Toast.LENGTH_LONG).show();
                                userPassword.getText().clear();
                                passwordText.getText().clear();
                                confirmPasswordText.getText().clear();
                            }
                        }
                    } else {
                        if (password.isEmpty() || confirmedPassword.isEmpty()) {
                            Toast.makeText(this,
                                            getString(R.string.empty_fields), Toast.LENGTH_LONG).
                                    show();
                            passwordText.getText().clear();
                            confirmPasswordText.getText().clear();
                        } else {
                            if (password.equals(confirmedPassword)) {
                                if (db.insertPassword(password)) {
                                    Toast.makeText(this, getString(R.string.password_added),
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(this,
                                            getString(R.string.impossible_add_password),
                                            Toast.LENGTH_LONG).show();
                                }
                                dialog.dismiss();
                            } else {
                                Toast.makeText(this,
                                        getString(R.string.passwords_not_match),
                                        Toast.LENGTH_LONG).show();
                                passwordText.getText().clear();
                                confirmPasswordText.getText().clear();
                            }
                        }
                    }
                });
            });
            dialog.show();
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
