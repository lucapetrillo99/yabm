package com.ilpet.yabm.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ilpet.yabm.R;
import com.ilpet.yabm.adapters.CategoriesAdapter;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.PasswordManagerDialog;
import com.ilpet.yabm.utils.PasswordDialog;
import com.ilpet.yabm.utils.SettingsManager;
import com.ilpet.yabm.utils.StoragePermissionDialog;

import java.util.ArrayList;
import java.util.function.Predicate;

public class CategoriesActivity extends AppCompatActivity implements View.OnLongClickListener {
    public static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int DELETE_OPTION = 1;
    private static final int LOCK_UNLOCK_OPTION = 2;
    public boolean areAllSelected = false;
    public boolean isContextualMenuEnable = false;
    private int counter = 0;
    private RecyclerView recyclerView;
    private CategoriesAdapter categoriesAdapter;
    private Toolbar toolbar, contextualToolbar;
    private TextView toolbarTitle;
    private ImageView sortOptions;
    private ArrayList<Category> categories;
    private ArrayList<Category> selectedCategories;
    private SettingsManager settingsManager;
    private FloatingActionButton insertCategory;
    private DatabaseHandler db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        toolbar = findViewById(R.id.toolbar);
        contextualToolbar = findViewById(R.id.contextual_toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.categories_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        sortOptions = findViewById(R.id.category_options_sort);
        recyclerView = findViewById(R.id.recycler_view);
        insertCategory = findViewById(R.id.add_button);

        db = DatabaseHandler.getInstance(getApplicationContext());
        settingsManager = new SettingsManager(getApplicationContext(), "category");
        categories = db.getCategories(settingsManager.getCategoryOrderBy(), settingsManager.getCategoryOrderType());
        selectedCategories = new ArrayList<>();
        setAdapter();
        setSortOptions();
        insertCategory.setOnClickListener(view -> categoriesAdapter.newCategoryDialog(0, false, view));
    }

    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    private void setSortOptions() {
        sortOptions.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(this, sortOptions);
            popup.getMenuInflater().inflate(R.menu.sort_options_menu, popup.getMenu());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                popup.setForceShowIcon(true);
            }

            if (settingsManager.getCategoryOrderBy().equals(String.valueOf(SettingsManager.SortOrder.date)) &&
                    settingsManager.getCategoryOrderType().equals(String.valueOf(SettingsManager.SortOrder.ASC))) {
                popup.getMenu().getItem(0).setChecked(true);
            } else if (settingsManager.getCategoryOrderBy().equals(String.valueOf(SettingsManager.SortOrder.date)) &&
                    settingsManager.getCategoryOrderType().equals(String.valueOf(SettingsManager.SortOrder.DESC))) {
                popup.getMenu().getItem(1).setChecked(true);
            } else if (settingsManager.getCategoryOrderBy().equals(String.valueOf(SettingsManager.SortOrder.title)) &&
                    settingsManager.getCategoryOrderType().equals(String.valueOf(SettingsManager.SortOrder.ASC))) {
                popup.getMenu().getItem(2).setChecked(true);
            } else if (settingsManager.getCategoryOrderBy().equals(String.valueOf(SettingsManager.SortOrder.title)) &&
                    settingsManager.getCategoryOrderType().equals(String.valueOf(SettingsManager.SortOrder.DESC))) {
                popup.getMenu().getItem(3).setChecked(true);
            }

            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                switch (id) {
                    case R.id.date_ascending:
                        categories.sort(Category.DateAscendingOrder);
                        item.setChecked(!item.isChecked());
                        settingsManager.setCategoryOrderBy(SettingsManager.SortOrder.date);
                        settingsManager.setCategoryOrderType(SettingsManager.SortOrder.ASC);
                        categoriesAdapter.notifyDataSetChanged();
                        break;
                    case R.id.date_descending:
                        categories.sort(Category.DateDescendingOrder);
                        item.setChecked(!item.isChecked());
                        settingsManager.setCategoryOrderBy(SettingsManager.SortOrder.date);
                        settingsManager.setCategoryOrderType(SettingsManager.SortOrder.DESC);
                        categoriesAdapter.notifyDataSetChanged();
                        break;
                    case R.id.title_ascending:
                        categories.sort(Category.TitleAscendingOrder);
                        item.setChecked(!item.isChecked());
                        settingsManager.setCategoryOrderBy(SettingsManager.SortOrder.title);
                        settingsManager.setCategoryOrderType(SettingsManager.SortOrder.ASC);
                        categoriesAdapter.notifyDataSetChanged();
                        break;
                    case R.id.title_descending:
                        categories.sort(Category.TitleDescendingOrder);
                        item.setChecked(!item.isChecked());
                        settingsManager.setCategoryOrderBy(SettingsManager.SortOrder.title);
                        settingsManager.setCategoryOrderType(SettingsManager.SortOrder.DESC);
                        categoriesAdapter.notifyDataSetChanged();
                        break;
                }
                return true;
            });
            popup.show();
        });
    }

    private void setAdapter() {
        categoriesAdapter = new CategoriesAdapter(categories, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoriesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem settings = menu.findItem(R.id.settings);
        settings.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.settings:
                Intent activityIntent = new Intent(CategoriesActivity.this, SettingsActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                break;
            case R.id.search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        categoriesAdapter.getFilter().filter(newText);
                        return false;
                    }
                });
                break;
        }
        return true;
    }

    public void makeSelection(View v, int position) {
        if (((CheckBox) v).isChecked()) {
            selectedCategories.add(categories.get(position));
            counter++;
        } else {
            selectedCategories.remove(categories.get(position));
            counter--;
        }
        updateCounter();
    }

    public void updateCounter() {
        toolbarTitle.setText(String.valueOf(counter));
    }

    private void removeContextualActionMode() {
        isContextualMenuEnable = false;
        areAllSelected = false;
        toolbarTitle.setText(R.string.categories_title);
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
        toolbar.inflateMenu(R.menu.menu);
        contextualToolbar.setVisibility(View.GONE);
        insertCategory.setVisibility(View.VISIBLE);
        counter = 0;
        selectedCategories.clear();
        categoriesAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onLongClick(View v) {
        if (categories.size() > 1) {
            isContextualMenuEnable = true;
            final boolean[] lock = {false};
            insertCategory.setVisibility(View.GONE);
            contextualToolbar.setVisibility(View.VISIBLE);
            ImageButton move = contextualToolbar.findViewById(R.id.move);
            ImageButton delete = contextualToolbar.findViewById(R.id.delete);
            ImageButton archive = contextualToolbar.findViewById(R.id.archive);
            ImageButton unarchive = contextualToolbar.findViewById(R.id.unarchive);
            ImageButton selectAll = contextualToolbar.findViewById(R.id.select_all);
            ImageView protection = contextualToolbar.findViewById(R.id.handle_password);
            archive.setVisibility(View.GONE);
            unarchive.setVisibility(View.GONE);
            move.setVisibility(View.GONE);

            delete.setOnClickListener(v12 -> {
                if (counter > 0) {
                    confirmDeletionDialog();
                }
            });
            toolbar.setNavigationIcon(R.drawable.ic_back_button);
            toolbar.setNavigationOnClickListener(v1 -> removeContextualActionMode());
            categoriesAdapter.notifyDataSetChanged();

            selectAll.setOnClickListener(v12 -> {
                if (!areAllSelected) {
                    areAllSelected = true;
                    selectedCategories.addAll(categories);
                    Predicate<Category> pr = a -> (a.getCategoryTitle().equals(
                            getString(R.string.default_bookmarks)));
                    selectedCategories.removeIf(pr);
                    counter = categories.size() - 1;
                    int protectionCounter = 0;
                    for (Category category : selectedCategories) {
                        if (category.getPasswordProtection() == Category.CategoryProtection.LOCK) {
                            protectionCounter += 1;
                            lock[0] = true;
                        } else {
                            protectionCounter = 0;
                            lock[0] = false;
                        }
                    }
                    if (protectionCounter > 0) {
                        protection.setImageResource(R.drawable.ic_unlock);
                    } else {
                        protection.setImageResource(R.drawable.ic_lock);
                    }
                } else {
                    areAllSelected = false;
                    selectedCategories.removeAll(categories);
                    counter = 0;
                }
                updateCounter();
                categoriesAdapter.notifyDataSetChanged();
            });

            protection.setOnClickListener(v12 -> {
                if (counter > 0) {
                    String currentPassword = db.getPassword();
                    if (currentPassword == null) {
                        PasswordManagerDialog passwordManagerDialog = new PasswordManagerDialog(
                                this);
                        passwordManagerDialog.createDialog();
                    } else {
                        if (lock[0]) {
                            PasswordDialog passwordDialog = new PasswordDialog(this,
                                    result -> {
                                        if (result) {
                                            categoriesAdapter.updateCategories(
                                                    selectedCategories, LOCK_UNLOCK_OPTION,
                                                    Category.CategoryProtection.UNLOCK);

                                            removeContextualActionMode();
                                            Toast.makeText(this,
                                                    "Categorie aggiornate!",
                                                    Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(this,
                                                    getString(R.string.wrong_password),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                            passwordDialog.show(getSupportFragmentManager(), "Password dialog");
                        } else {
                            categoriesAdapter.updateCategories(
                                    selectedCategories, LOCK_UNLOCK_OPTION,
                                    Category.CategoryProtection.LOCK);
                            removeContextualActionMode();
                            Toast.makeText(this,
                                    getString(R.string.categories_updated),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
        }
        return true;
    }

    private void confirmDeletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message;
        String categoryQuestion;
        String deletedQuestion;
        String categoryMessage;

        message = "Sei sicuro di voler eliminare ";
        if (counter > 1) {
            categoryQuestion = " categorie?";
            deletedQuestion = " eliminate!";
            categoryMessage = "Categorie";
        } else {
            categoryQuestion = " categoria?";
            deletedQuestion = " eliminata!";
            categoryMessage = "Categoria";
        }
        builder.setMessage(message + counter + categoryQuestion + "\n" +
                        getString(R.string.all_bookmarks_deleted))
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    categoriesAdapter.updateCategories(selectedCategories, DELETE_OPTION, null);
                    Toast.makeText(getApplicationContext(), categoryMessage + deletedQuestion,
                            Toast.LENGTH_LONG).show();
                    removeContextualActionMode();
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                categoriesAdapter.closeCategoryDialog();
                StoragePermissionDialog storagePermissionDialog = new StoragePermissionDialog(this);
                storagePermissionDialog.showWarningMessage();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}