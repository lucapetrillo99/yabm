package com.ilpet.yabm.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.adapters.BookmarksAdapter;
import com.ilpet.yabm.adapters.CategoriesMenuAdapter;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.R;
import com.ilpet.yabm.utils.SettingsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener {
    private static final int DELETE_OPTION = 1;
    private static final int ARCHIVE_OPTION = 2;
    private static final int UNARCHIVE_OPTION = 3;
    private static final String CATEGORY = "category";
    private Intent activityIntent;
    private DatabaseHandler db;
    private ArrayList<Bookmark> bookmarks;
    private RecyclerView recyclerView;
    private BookmarksAdapter bookmarksAdapter;
    private RecyclerView categoriesRecyclerview;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private DrawerLayout drawerLayout;
    private TextView noBookmarks;
    private FloatingActionButton fab;
    private ArrayList<Category> categories;
    private ArrayList<Bookmark> archivedUrl;
    private ArrayList<Bookmark> removedFromArchive;
    private ArrayList<Bookmark> selectedBookmarks;
    private int counter = 0;
    private String previousCategory;
    public boolean isContextualMenuEnable = false;
    public boolean areAllSelected = false;
    public boolean isArchiveModeEnabled = false;
    private static final int SYSTEM_DEFAULT = 0;
    private static final int LIGHT_MODE = 1;
    private static final int NIGHT_MODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme();
        db = DatabaseHandler.getInstance(getApplicationContext());
        SettingsManager settingsManager = new SettingsManager(getApplicationContext(), CATEGORY);
        String result = settingsManager.getCategory();
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        drawerLayout = findViewById(R.id.drawer_layout);
        categoriesRecyclerview = findViewById(R.id.categories_recyclerview);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu);
        }
        noBookmarks = findViewById(R.id.no_bookmarks);
        recyclerView = findViewById(R.id.recycler_view);

        if (settingsManager.isFirstAccess()) {
            Category defaultCategory = new Category(null, getString(R.string.default_bookmarks), null);
            Category archiveCategory = new Category(null, getString(R.string.archived_bookmarks), null);
            ArrayList<Category> appCategories = new ArrayList<>();
            appCategories.add(defaultCategory);
            appCategories.add(archiveCategory);
            for (Category category : appCategories) {
                db.addCategory(category);
            }
            File folder = new File(getFilesDir() + File.separator +
                    getString(R.string.app_name));
            boolean success = true;
            if (!folder.exists()) {
                success = folder.mkdirs();
            }
            if (!success) {
                Toast.makeText(getApplicationContext(), "Qualcosa è andato storto!",
                        Toast.LENGTH_LONG).show();
            }
            settingsManager.setFirstAccess(false);
        }

        if (result.equals(getString(R.string.all_bookmarks_title))) {
            bookmarks = new ArrayList<>(db.getAllBookmarks());
        } else {
            bookmarks = new ArrayList<>(db.getBookmarksByCategory(result));
        }
        toolbarTitle.setText(result);
        previousCategory = result;
        archiveUrl();
        unarchiveBookmark();

        archivedUrl = new ArrayList<>();
        selectedBookmarks = new ArrayList<>();
        removedFromArchive = new ArrayList<>();
        categories = db.getAllCategories();
        setAdapter();

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                handleSendText(intent);
            }
        }

        fab = findViewById(R.id.add_button);
        fab.setOnClickListener(view -> {
            previousCategory = toolbarTitle.getText().toString();
            activityIntent = new Intent(MainActivity.this, InsertBookmarkActivity.class);
            startActivity(activityIntent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            if (isContextualMenuEnable) {
                removeContextualActionMode();
            }
        });

        setBookmarksLabel();
    }

    private void setAdapter() {
        setBookmarksLabel();
        initSwipe();
        bookmarksAdapter = new BookmarksAdapter(bookmarks, MainActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(bookmarksAdapter);
    }

    void handleSendText (Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Intent linkIntent = new Intent(MainActivity.this, InsertBookmarkActivity.class);
            linkIntent.putExtra("url", sharedText);
            startActivity(linkIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        addFilterCategories();
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                activityIntent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(activityIntent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                if (isContextualMenuEnable) {
                    removeContextualActionMode();
                }
                break;
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        bookmarksAdapter.getFilter().filter(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
                searchView.setOnQueryTextFocusChangeListener((view, queryTextFocused) -> {
                    if(!queryTextFocused) {
                        if (previousCategory.equals(getString(R.string.all_bookmarks_title))) {
                            bookmarks.clear();
                            bookmarks = db.getAllBookmarks();
                            setAdapter();
                        } else {
                            bookmarks.clear();
                            bookmarks = db.getBookmarksByCategory(previousCategory);
                            setAdapter();
                        }
                    }
                });
                break;
            case R.id.delete:
                if (counter > 0) {
                    contextualModeDialog(DELETE_OPTION);
                }
                break;
            case R.id.archive:
                if (counter > 0) {
                    contextualModeDialog(ARCHIVE_OPTION);
                }
                break;
            case R.id.unarchive:
                if (counter > 0) {
                    contextualModeDialog(UNARCHIVE_OPTION);
                }
                break;
            case R.id.select_all:
                if (!areAllSelected) {
                    areAllSelected = true;
                    selectedBookmarks.addAll(bookmarks);
                    counter = bookmarks.size();
                } else {
                    areAllSelected = false;
                    selectedBookmarks.removeAll(bookmarks);
                    counter = 0;
                }
                updateCounter();
                bookmarksAdapter.notifyDataSetChanged();
                break;
        }
        return true;
    }

    public void initSwipe() {

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAbsoluteAdapterPosition();

                switch (direction) {
                    case ItemTouchHelper.LEFT:
                        if(toolbarTitle.getText().toString().equals(getString(R.string.archived_bookmarks))) {
                            Bookmark unarchivedBookmark = bookmarks.get(position);
                            removedFromArchive.add(bookmarks.get(position));
                            bookmarksAdapter.removeBookmark(position);
                            bookmarks.remove(position);
                            bookmarksAdapter.notifyItemRemoved(position);

                            Snackbar.make(recyclerView, unarchivedBookmark.getLink() + " rimosso dall'archivio.", Snackbar.LENGTH_LONG)
                                    .setAction("Annulla", v -> {
                                        removedFromArchive.remove(removedFromArchive.lastIndexOf(unarchivedBookmark));
                                        bookmarks.add(position, unarchivedBookmark);
                                        bookmarksAdapter.notifyItemInserted(position);
                                    }).show();
                        } else {
                            Bookmark archivedBookmark = bookmarks.get(position);
                            archivedUrl.add(bookmarks.get(position));
                            bookmarks.remove(position);
                            bookmarksAdapter.removeBookmark(position);
                            bookmarksAdapter.notifyItemRemoved(position);

                            Snackbar.make(recyclerView, archivedBookmark.getLink() + " archiviato.", Snackbar.LENGTH_LONG)
                                    .setAction("Annulla", v -> {
                                        archivedUrl.remove(archivedUrl.lastIndexOf(archivedBookmark));
                                        bookmarks.add(position, archivedBookmark);
                                        bookmarksAdapter.notifyItemInserted(position);
                                    }).show();
                        }
                        break;
                    case ItemTouchHelper.RIGHT:
                        confirmDialog(bookmarks.get(position).getId(), position);
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                if(toolbarTitle.getText().toString().equals(getString(R.string.archived_bookmarks))) {
                    new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                            .addSwipeRightActionIcon(R.drawable.ic_actions)
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                            .addSwipeLeftActionIcon(R.drawable.ic_unarchive)
                            .create()
                            .decorate();

                } else {
                    new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                            .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                            .addSwipeRightActionIcon(R.drawable.ic_actions)
                            .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                            .addSwipeLeftActionIcon(R.drawable.ic_archive)
                            .create()
                            .decorate();

                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }


    private void confirmDialog(String id, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler eliminare il link?")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    bookmarksAdapter.notifyDataSetChanged();
                })
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    if (db.deleteBookmark(id)) {
                        bookmarks.remove(position);
                        bookmarksAdapter.removeBookmark(position);
                        bookmarksAdapter.notifyItemRemoved(position);
                        Toast.makeText(getApplicationContext(), "Segnalibro eliminato", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Impossibile eliminare il segnalibro", Toast.LENGTH_LONG).show();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void archiveUrl() {
        if (archivedUrl != null) {
            if (archivedUrl.size() > 0){
                for (int i = 0; i < archivedUrl.size(); i++)
                    db.addToArchive(archivedUrl.get(i).getId(), archivedUrl.get(i).getCategory());
            }
        }
    }

    public void unarchiveBookmark() {
        if (removedFromArchive != null) {
            if (removedFromArchive.size() > 0){
                for (int i = 0; i < removedFromArchive.size(); i++)
                    db.removeFromArchive(removedFromArchive.get(i).getId());
            }
        }
    }

    public void onResume() {
        super.onResume();
        archiveUrl();
        setBookmarksLabel();
        if (previousCategory != null) {
            if (previousCategory.equals(getString(R.string.all_bookmarks_title))) {
                bookmarks = db.getAllBookmarks();
            } else {
                bookmarks = db.getBookmarksByCategory(previousCategory);
            }
            setAdapter();
        }
        categories.clear();
        categories = db.getAllCategories();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onLongClick(View v) {
        previousCategory = toolbarTitle.getText().toString();
        isContextualMenuEnable = true;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.contextual_menu);
        MenuItem archive = toolbar.getMenu().findItem(R.id.archive);
        MenuItem unarchive = toolbar.getMenu().findItem(R.id.unarchive);
        if (previousCategory.equals(getString(R.string.archived_bookmarks))) {
            archive.setVisible(false);
            unarchive.setVisible(true);
        } else {
            archive.setVisible(true);
            unarchive.setVisible(false);
        }
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        toolbar.setNavigationOnClickListener(v1 -> removeContextualActionMode());
        bookmarksAdapter.notifyDataSetChanged();
        return true;
    }

    public void makeSelection(View view, int position) {
        if (((CheckBox)view).isChecked()) {
            selectedBookmarks.add(bookmarks.get(position));
            counter ++;
        } else {
            selectedBookmarks.remove(bookmarks.get(position));
            counter --;
        }
        updateCounter();
    }

    public void updateCounter() {
        toolbarTitle.setText(String.valueOf(counter));
    }

    public void removeContextualActionMode() {
        isContextualMenuEnable = false;
        areAllSelected = false;
        toolbarTitle.setText(previousCategory);
        toolbar.getMenu().clear();
        toolbar.setNavigationIcon(null);
        toolbar.inflateMenu(R.menu.menu);
        addFilterCategories();
        counter = 0;
        selectedBookmarks.clear();
        bookmarksAdapter.notifyDataSetChanged();

    }

    private void addFilterCategories() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        categoriesRecyclerview.setLayoutManager(layoutManager);
        categoriesRecyclerview.setHasFixedSize(true);
        CategoriesMenuAdapter categoriesMenuAdapter = new CategoriesMenuAdapter(categories, this, toolbarTitle.getText().toString());
        categoriesRecyclerview.setAdapter(categoriesMenuAdapter);
    }

    @Override
    public void onBackPressed() {
        if (isContextualMenuEnable) {
            removeContextualActionMode();
        } else {
            finish();
        }
    }

    private void contextualModeDialog(int operation) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String message = null;
        String bookmarkQuestion = null;
        String deletedQuestion = null;
        String bookmarkMessage = null;

        switch (operation) {
            case DELETE_OPTION:
                message = "Sei sicuro di voler eliminare ";
                if (counter > 1) {
                    bookmarkQuestion = " segnalibri?";
                    deletedQuestion = " eliminati!";
                    bookmarkMessage = "Segnalibri";
                } else {
                    bookmarkQuestion = " segnalibro?";
                    deletedQuestion = " eliminato!";
                    bookmarkMessage = "Segnalibro";
                }
                break;
            case ARCHIVE_OPTION:
                message = "Sei sicuro di voler archiviare ";
                if (counter > 1) {
                    bookmarkQuestion = " segnalibri?";
                    deletedQuestion = " archiviati!";
                    bookmarkMessage = "Segnalibri";
                } else {
                    bookmarkQuestion = " segnalibro?";
                    deletedQuestion = " archiviato!";
                    bookmarkMessage = "Segnalibro";
                }
                break;
            case UNARCHIVE_OPTION:
                message = "Sei sicuro di voler ripristinare ";
                if (counter > 1) {
                    bookmarkQuestion = " segnalibri?";
                    deletedQuestion = " ripristinati!";
                    bookmarkMessage = "Segnalibri";
                } else {
                    bookmarkQuestion = " segnalibro?";
                    deletedQuestion = " ripristinato!";
                    bookmarkMessage = "Segnalibro";
                }
                break;
        }
        String finalBookmarkMessage = bookmarkMessage;
        String finalDeletedQuestion = deletedQuestion;
        builder.setMessage(message + counter + bookmarkQuestion)
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    switch (operation) {
                        case DELETE_OPTION:
                            bookmarksAdapter.updateBookmarks(selectedBookmarks, DELETE_OPTION);
                            break;
                        case ARCHIVE_OPTION:
                            bookmarksAdapter.updateBookmarks(selectedBookmarks, ARCHIVE_OPTION);
                            break;
                        case UNARCHIVE_OPTION:
                            bookmarksAdapter.updateBookmarks(selectedBookmarks, UNARCHIVE_OPTION);
                            break;
                    }
                    Toast.makeText(getApplicationContext(), finalBookmarkMessage + finalDeletedQuestion,
                            Toast.LENGTH_LONG).show();
                    removeContextualActionMode();

                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setBookmarksLabel(){
        if (bookmarks.size() == 0) {
            noBookmarks.setVisibility(View.VISIBLE);
        } else {
            noBookmarks.setVisibility(View.INVISIBLE);
        }
    }

    public void setAppTheme() {
        SettingsManager settingsManager = new SettingsManager(this, "theme");
        int theme = settingsManager.getTheme();
        switch (theme) {
            case NIGHT_MODE:
                settingsManager.setTheme(NIGHT_MODE);
                break;
            case LIGHT_MODE:
                settingsManager.setTheme(LIGHT_MODE);
                break;
            default:
                settingsManager.setTheme(SYSTEM_DEFAULT);
        }
    }

    public void filterByCategory(String categoryName) {
         if (categoryName.equals(getString(R.string.all_bookmarks_title))){
            toolbarTitle.setText(categoryName);
            previousCategory = categoryName;
            fab.setVisibility(View.VISIBLE);
            archiveUrl();
            unarchiveBookmark();
            bookmarks.clear();
            bookmarks = db.getAllBookmarks();
         } else {
             if (categoryName.equals(getString(R.string.archived_bookmarks))) {
                 isArchiveModeEnabled = true;
                 fab.setVisibility(View.INVISIBLE);
             } else {
                 isArchiveModeEnabled = false;
                 fab.setVisibility(View.VISIBLE);
             }
             toolbarTitle.setText(categoryName);
             previousCategory = categoryName;
             archiveUrl();
             unarchiveBookmark();
             bookmarks.clear();
             bookmarks = db.getBookmarksByCategory(categoryName);
         }
        setAdapter();
        drawerLayout.closeDrawers();
    }
}