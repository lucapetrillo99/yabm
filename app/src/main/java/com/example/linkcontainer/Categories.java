package com.example.linkcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class Categories extends AppCompatActivity implements View.OnLongClickListener {
    public boolean areAllSelected = false;
    public boolean isContextualMenuEnable = false;
    private RecyclerView recyclerView;
    private CategoriesAdapter categoriesAdapter;
    private DatabaseHandler db;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private ArrayList<String> categories;
    private ArrayList<String> selectedCategories;
    private int counter = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Categorie");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton insertCategory = findViewById(R.id.add_button);

        db = DatabaseHandler.getInstance(getApplicationContext());
        categories = db.getCategories();
        selectedCategories = new ArrayList<>();
        setAdapter();

        insertCategory.setOnClickListener(view -> {
            categoriesAdapter.createDialog(0, false, view);
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
        MenuItem filter = menu.findItem(R.id.filter);
        filter.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.settings:
                Intent activityIntent = new Intent(Categories.this, SettingsActivity.class);
                startActivity(activityIntent);
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
            case R.id.delete:
                if (counter > 0) {
                    contextualModeDialog();
                }
                break;
            case R.id.select_all:
                if (!areAllSelected) {
                    areAllSelected = true;
                    selectedCategories.addAll(categories);
                    counter = categories.size() - 1;
                } else {
                    areAllSelected = false;
                    selectedCategories.removeAll(categories);
                    counter = 0;
                }
                updateCounter();
                categoriesAdapter.notifyDataSetChanged();
        }
        return true;
    }

    public void makeSelection(View v, int position) {
        if (((CheckBox)v).isChecked()) {
            selectedCategories.add(categories.get(position));
            counter ++;
        } else {
            selectedCategories.remove(categories.get(position));
            counter --;
        }
        updateCounter();
    }

    public void updateCounter() {
        toolbarTitle.setText(String.valueOf(counter));
    }

    @SuppressLint("SetTextI18n")
    private void removeContextualActionMode() {
        isContextualMenuEnable = false;
        areAllSelected = false;
        toolbarTitle.setText("Categorie");
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.inflateMenu(R.menu.menu);
        counter = 0;
        selectedCategories.clear();
        categoriesAdapter.notifyDataSetChanged();

    }

    @Override
    public boolean onLongClick(View v) {
        if (categories.size() > 1) {
            isContextualMenuEnable = true;
            toolbar.getMenu().clear();
            toolbar.inflateMenu(R.menu.contextual_menu);
            MenuItem archive = toolbar.getMenu().findItem(R.id.archive);
            MenuItem unarchive = toolbar.getMenu().findItem(R.id.unarchive);
            archive.setVisible(false);
            unarchive.setVisible(false);
            toolbar.setNavigationIcon(R.drawable.ic_back_button);
            toolbar.setNavigationOnClickListener(v1 -> removeContextualActionMode());
            categoriesAdapter.notifyDataSetChanged();
        }

        return false;
    }

    private void contextualModeDialog() {
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

        String finalBookmarkMessage = categoryMessage;
        String finalDeletedQuestion = deletedQuestion;
        builder.setMessage(message + counter + categoryQuestion)
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    categoriesAdapter.updateCategories(selectedCategories);
                    Toast.makeText(getApplicationContext(), finalBookmarkMessage + finalDeletedQuestion,
                            Toast.LENGTH_LONG).show();
                    removeContextualActionMode();

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}