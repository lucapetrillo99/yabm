package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collection;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements Filterable, View.OnLongClickListener {
    private Intent activityIntent;
    private DatabaseHandler db;
    private ArrayList<Bookmark> bookmarks;
    private RecyclerView recyclerView;
    private RecyclerAdapter recyclerAdapter;
    private Toolbar toolbar;
    private TextView toolbarTitle;
    private FloatingActionButton fab;
    private ArrayList<String> categories;
    private ArrayList<Bookmark> allBookmarks;
    private ArrayList<Bookmark> archivedUrl;
    private ArrayList<Bookmark> selectedBookmarks;
    public boolean isContextualMenuEnable = false;
    private int counter = 0;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Tutti i segnalibri");
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_view);

        db = DatabaseHandler.getInstance(getApplicationContext());
        archiveUrl();
        bookmarks = new ArrayList<>(db.getAllBookmarks());
        archivedUrl = new ArrayList<>();
        allBookmarks = new ArrayList<>(bookmarks);
        selectedBookmarks = new ArrayList<>();
        categories = db.getAllCategories();
        setAdapter();
        initSwipe((String) toolbar.getTitle());

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
            activityIntent = new Intent(MainActivity.this, InsertLink.class);
            startActivity(activityIntent);
        });

    }

    private void setAdapter() {
        recyclerAdapter = new RecyclerAdapter(bookmarks, MainActivity.this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(recyclerAdapter);
    }

    void handleSendText (Intent intent){
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            Intent linkIntent = new Intent(MainActivity.this, InsertLink.class);
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
            case R.id.categories:
                activityIntent = new Intent(MainActivity.this, Categories.class);
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
                        getFilter().filter(newText);
                        return false;
                    }
                });
                break;
            case R.id.delete:
                recyclerAdapter.updateBookmarks(selectedBookmarks, 1);
                removeContextualActionMode();
                break;
            case R.id.archive:
                recyclerAdapter.updateBookmarks(selectedBookmarks, 2);
                removeContextualActionMode();
                break;
            case R.id.share:
                break;
        }

        for (String category: categories) {
            if(item.getTitle() == category) {
                if(item.getTitle().equals("Archiviati")) {
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                }
                toolbarTitle.setText(item.getTitle());
                archiveUrl();
                bookmarks.clear();
                bookmarks = db.getBookmarksByCategory((String)item.getTitle());
                setAdapter();
            } else if (item.getTitle().equals("Tutti i segnalibri")){
                toolbarTitle.setText(item.getTitle());
                fab.setVisibility(View.VISIBLE);
                archiveUrl();
                bookmarks.clear();
                bookmarks = db.getAllBookmarks();
                setAdapter();
            }
        }
        return true;
    }

    public void initSwipe(String pageCategory) {
        int swapDirs = ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;

        if (pageCategory.equals("Archiviati")) {
            swapDirs = ItemTouchHelper.RIGHT;
        }

        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, swapDirs) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

                int position = viewHolder.getAdapterPosition();

                switch (direction) {
                    case ItemTouchHelper.LEFT:

                        Bookmark archivedBookmark = bookmarks.get(position);
                        archivedUrl.add(bookmarks.get(position));
                        bookmarks.remove(position);
                        recyclerAdapter.notifyItemRemoved(position);

                        Snackbar.make(recyclerView, archivedBookmark.getLink() + " archiviato.", Snackbar.LENGTH_LONG)
                                .setAction("Annulla", v -> {
                                    archivedUrl.remove(archivedUrl.lastIndexOf(archivedBookmark));
                                    bookmarks.add(position, archivedBookmark);
                                    recyclerAdapter.notifyItemInserted(position);
                                }).show();

                        break;
                    case ItemTouchHelper.RIGHT:
                        confirmDialog(bookmarks.get(position).getId(), position);
                        break;
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(MainActivity.this, c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                        .addSwipeRightActionIcon(R.drawable.ic_actions)
                        .addSwipeLeftBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.design_default_color_primary_dark))
                        .addSwipeLeftActionIcon(R.drawable.ic_archive)
                        .create()
                        .decorate();

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
                    recyclerAdapter.notifyDataSetChanged();
                })
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    if (db.deleteBookmark(id)) {
                        bookmarks.remove(position);
                        allBookmarks.remove(position);
                        recyclerAdapter.notifyItemRemoved(position);
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
                    db.addToArchive(archivedUrl.get(i).getId());
            }
        }
    }

    public void onResume() {
        super.onResume();
        archiveUrl();
        bookmarks.clear();
        bookmarks = db.getAllBookmarks();
        initSwipe((String) toolbar.getTitle());
        setAdapter();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter(){
        @Override
        protected Filter.FilterResults performFiltering(CharSequence constraint) {
            ArrayList<Bookmark> filteredBookmarks = new ArrayList<>();

            if (constraint.toString().isEmpty()) {
                filteredBookmarks.addAll(allBookmarks);
            } else {
                for (Bookmark bookmark: allBookmarks) {
                    if (bookmark.getLink().toLowerCase().contains(constraint.toString().toLowerCase())
                    || bookmark.getTitle().toLowerCase().contains(constraint.toString().toLowerCase())
                    || bookmark.getDescription().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        filteredBookmarks.add(bookmark);
                    }
                }
            }

            FilterResults filterResults = new FilterResults();
            filterResults.values = filteredBookmarks;

            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            bookmarks.clear();
            bookmarks.addAll((Collection<? extends Bookmark>) results.values);
            recyclerAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public boolean onLongClick(View v) {
        isContextualMenuEnable = true;
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.contextual_menu);
        recyclerAdapter.notifyDataSetChanged();

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

    @SuppressLint("SetTextI18n")
    private void removeContextualActionMode() {

        isContextualMenuEnable = false;
        toolbarTitle.setText("Tutti i segnalibri");
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.menu);
        addFilterCategories();
        counter = 0;
        selectedBookmarks.clear();
        recyclerAdapter.notifyDataSetChanged();

    }

    private void addFilterCategories() {
        toolbar.getMenu().addSubMenu(Menu.NONE, R.id.filter, Menu.NONE,"Menu1");

        SubMenu subMenu = toolbar.getMenu().findItem(R.id.filter).getSubMenu();
        subMenu.clear();
        for (int i = 0; i < categories.size(); i ++) {
            subMenu.add(0, i + 1, Menu.NONE, categories.get(i));
        }

    }
}