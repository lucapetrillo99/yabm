package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import at.blogc.android.views.ExpandableTextView;

public class BookmarksManagerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final String IMPORTED = "Importati";
    private static final long ALARM_START_TIME = -1;
    private RelativeLayout importOption;
    private ExpandableTextView importOptionText;
    private TextView description;
    private Button selectButton;
    private final Set<String> importedBookmarks = new LinkedHashSet<>();
    private int bookmarksCounter = 0;
    private DatabaseHandler db;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Importa/Esporta");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        db = DatabaseHandler.getInstance(getApplicationContext());

        importOption =  findViewById(R.id.import_option);
        importOptionText = findViewById(R.id.expandableTextView);
        selectButton = findViewById(R.id.select_button);
        description = findViewById(R.id.description);
        importOptionText.setInterpolator(new OvershootInterpolator());

        importOptionText.setExpandInterpolator(new OvershootInterpolator());
        importOptionText.setCollapseInterpolator(new OvershootInterpolator());
        importOptionText.setAnimationDuration(750L);

        importOptionTextListener();
        selectButtonListener();
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void importOptionTextListener() {
        importOption.setOnClickListener(v -> {
            if (importOptionText.isExpanded()) {
                importOptionText.collapse();
                description.setVisibility(View.GONE);
                selectButton.setVisibility(View.GONE);
            }
            else {
                importOptionText.expand();
                description.setVisibility(View.VISIBLE);
                selectButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void selectButtonListener() {
        selectButton.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/html");

                startActivityForResult(intent, PERMISSION_REQUEST_STORAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERMISSION_REQUEST_STORAGE && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();
                getBookmarksFromUri(uri);
                importOptionsDialog();
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showMessage();
            }
        }
    }

    private void showMessage() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), ""
                , Snackbar.LENGTH_LONG);

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

    private void getBookmarksFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream =
                     getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Document document = Jsoup.parse(stringBuilder.toString());

        Elements links =  document.select("a[href]");

        for (Element element: links) {
            importedBookmarks.add(element.attr("href"));
        }
    }

    private void importOptionsDialog() {
        String question;
        if (importedBookmarks.size() > 1) {
            question = " segnalibri?";
        } else {
            question = " segnalibro?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksManagerActivity.this);
        builder.setTitle("Sei sicuro di voler importare " + importedBookmarks.size() + question);
        builder.setPositiveButton("Importa", (dialog, which) -> importBookmarks());
        builder.setNeutralButton("Annulla", (dialog, which) -> { });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void importBookmarks() {
        LoadingDialog loadingDialog = new LoadingDialog(BookmarksManagerActivity.this);
        loadingDialog.startLoading();
        String queryResult = db.getCategoryId(IMPORTED);
        String categoryId;
        if (queryResult == null) {
            db.addCategory(IMPORTED);
            categoryId = db.getCategoryId(IMPORTED);
        } else {
            categoryId = queryResult;
        }

        if (importedBookmarks.size() > 0) {
            Thread thread = new Thread() {
                public void run() {
                    for (String link : importedBookmarks) {
                        try {
                            Document document = Jsoup.connect(link).get();
                            if (document != null) {
                                Bookmark bookmark = new Bookmark();
                                Elements metaTags = document.getElementsByTag("meta");
                                for (Element element : metaTags) {
                                    if (element.attr("property").equals("og:image")) {
                                        bookmark.setImage(element.attr("content"));
                                    } else if (element.attr("property").equals("og:site_name")) {
                                        bookmark.setTitle(element.attr("content"));
                                    } else if (element.attr("name").equals("description")) {
                                        bookmark.setDescription(element.attr("content"));
                                    }
                                }
                                bookmark.setLink(link);
                                bookmark.setCategory(categoryId);
                                bookmark.setReminder(ALARM_START_TIME);

                                boolean insertionResult = db.addBookmark(bookmark);
                                if (insertionResult)
                                    bookmarksCounter++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    runOnUiThread(() -> {
                        loadingDialog.dismissLoading();
                        Toast.makeText(BookmarksManagerActivity.this, bookmarksCounter +
                                        " segnalibri importati!",
                                Toast.LENGTH_LONG).show();
                    });
                }
            };
            thread.start();
        } else {
            Toast.makeText(BookmarksManagerActivity.this, "Non ci sono segnalibri da importare!",
                    Toast.LENGTH_LONG).show();
        }
    }
}
