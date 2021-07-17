package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Categories extends AppCompatActivity implements View.OnLongClickListener {
    public boolean areAllSelected = false;
    public boolean isContextualMenuEnable = false;
    private RecyclerView recyclerView;
    private CategoriesAdapter categoriesAdapter;
    private DatabaseHandler db;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private ArrayList<Category> categories;
    private ArrayList<Category> selectedCategories;
    private int counter = 0;
    public static final int PERMISSION_REQUEST_STORAGE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Categorie");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(v -> finish());

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

    public void getImageFromDevice() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PERMISSION_REQUEST_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showWarningMessage();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PERMISSION_REQUEST_STORAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                categoriesAdapter.addImageTitle.setVisibility(View.GONE);
                categoriesAdapter.addImageButton.setVisibility(View.GONE);
                categoriesAdapter.categoryImage.setVisibility(View.VISIBLE);
                categoriesAdapter.imageLayout.setVisibility(View.VISIBLE);
                Uri chosenImageUri = data.getData();

                try {
                    categoriesAdapter.image = MediaStore.Images.
                            Media.getBitmap(this.getContentResolver(), chosenImageUri);
                    categoriesAdapter.categoryImage.setImageBitmap(categoriesAdapter.image);
                } catch (IOException e) {
                    Toast.makeText(getApplicationContext(), "Impossibile caricare l'immagine!",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void showWarningMessage() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "",
                Snackbar.LENGTH_LONG);

        View customView = getLayoutInflater().inflate(R.layout.snackbar_custom, null);
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