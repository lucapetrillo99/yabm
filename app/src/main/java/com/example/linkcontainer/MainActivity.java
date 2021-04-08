package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import androidx.appcompat.widget.SearchView;

import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.w3c.dom.Text;

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
    public boolean isContextualMenuEnable = false;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = findViewById(R.id.title);
        toolbarTitle.setText("Tutti i segnalibri");
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.recycler_view);

        db = DatabaseHandler.getInstance(getApplicationContext());
        archiveUrl();
        bookmarks = new ArrayList<>(db.getAllBookmarks());
        archivedUrl = new ArrayList<>();
        allBookmarks = new ArrayList<>(bookmarks);
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
        MenuItem filterMenu = menu.findItem(R.id.filter);
        SubMenu subMenu = filterMenu.getSubMenu();

        subMenu.add(0, 0,  Menu.NONE, "Tutti i segnalibri");
        categories = db.getAllCategories();
        for (int i = 0; i < categories.size(); i ++) {
            subMenu.add(0, i + 1, Menu.NONE, categories.get(i));
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.categories) {
            activityIntent = new Intent(MainActivity.this, Categories.class);
            startActivity(activityIntent);

        } else if (id == R.id.search) {
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
        }

        for (String category: categories) {
            if(item.getTitle() == category) {
                if(item.getTitle().equals("Archiviati")) {
                    fab.setVisibility(View.INVISIBLE);
                } else {
                    fab.setVisibility(View.VISIBLE);
                }
                toolbar.setTitle(item.getTitle());
                archiveUrl();
                bookmarks.clear();
                bookmarks = db.getBookmarksByCategory((String)item.getTitle());
                setAdapter();
            } else if (item.getTitle().equals("Tutti i segnalibri")){
                toolbar.setTitle(item.getTitle());
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
                        confirmDialog(bookmarks.get(position).getLink(), position);
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


    private void confirmDialog(String url, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler eliminare il link?")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.cancel();
                    recyclerAdapter.notifyDataSetChanged();
                })
                .setPositiveButton("Sì", (dialogInterface, i) -> {
                    if (db.deleteBookmark(url)) {
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
                    db.addToArchive(archivedUrl.get(i).getLink());
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

        return true;
    }
}