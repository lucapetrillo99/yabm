package com.example.linkcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;

public class Categories extends AppCompatActivity {
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private CategoriesAdapter categoriesAdapter;
    private DatabaseHandler db;
    private ArrayList<String> categories;
    private ArrayList<String> allCategories;
    private FloatingActionButton insertCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Default");
        recyclerView = findViewById(R.id.recycler_view);
        insertCategory = findViewById(R.id.add_button);

        db = DatabaseHandler.getInstance(getApplicationContext());
        categories = db.getCategories();
        allCategories = new ArrayList<>(categories);
        setAdapter();

        insertCategory.setOnClickListener(view -> {
            categoriesAdapter.createDialog(0, false, view);
        });
    }

    private void setAdapter() {
        categoriesAdapter = new CategoriesAdapter(categories, getApplicationContext());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(categoriesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem categories = menu.findItem(R.id.categories);
        MenuItem filter = menu.findItem(R.id.filter);
        categories.setVisible(false);
        filter.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
        return true;
    }

}