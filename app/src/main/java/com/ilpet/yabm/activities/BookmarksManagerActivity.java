package com.ilpet.yabm.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.LoadingDialog;
import com.ilpet.yabm.utils.StoragePermissionDialog;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class BookmarksManagerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final long ALARM_START_TIME = -1;
    private RelativeLayout importOption;
    private RelativeLayout exportOption;
    private final Set<String> importedBookmarks = new LinkedHashSet<>();
    private final Set<String> descriptions = new LinkedHashSet<>();
    private final ArrayList<Bookmark> bookmarks = new ArrayList<>();
    private int bookmarksCounter = 0;
    private DatabaseHandler db;
    int descriptionCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks_manager);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.bookmarks_manager_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        db = DatabaseHandler.getInstance(getApplicationContext());

        importOption =  findViewById(R.id.import_option);
        exportOption =  findViewById(R.id.export_option);

        importListener();
        exportListener();
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

    }

    ActivityResultLauncher<Intent> importBookmarksLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                getBookmarksFromUri(uri);
                importOptionsDialog();
            }
        }
    });

    private void importListener() {
        importOption.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/html");

                importBookmarksLauncher.launch(intent);
            }
        });
    }

    private void exportListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        } else {
            exportOption.setOnClickListener(v -> {
                ArrayList<Bookmark> bookmarks = db.getAllBookmarks();
                if (bookmarks.size() == 0) {
                    Toast.makeText(getApplicationContext(), "Non ci sono segnalibri da esportare", Toast.LENGTH_LONG)
                            .show();
                } else {
                    createBookmarksFile();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                StoragePermissionDialog storagePermissionDialog = new StoragePermissionDialog(this);
                storagePermissionDialog.showWarningMessage();
            }
        }
    }

    private void getBookmarksFromUri(Uri uri) {
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
           Toast.makeText(getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG)
                   .show();
        }
        Document document = Jsoup.parse(stringBuilder.toString());
        Elements links =  document.select("a[href]");

        for (Element element: links) {
            importedBookmarks.add(element.attr("href"));
            descriptions.add(element.text());
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
        String queryResult = db.getCategoryId(getString(R.string.imported));
        String categoryId;
        if (queryResult == null) {
            Category category = new Category();
            category.setCategoryTitle(getString(R.string.imported));
            db.addCategory(category);
            categoryId = db.getCategoryId(getString(R.string.imported));
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
                                if (bookmark.getTitle() == null) {
                                    bookmark.setTitle(link.split("//")[1].split("/")[0]);
                                }
                                if (bookmark.getDescription() == null) {
                                    if (descriptionCounter < descriptions.toArray().length) {
                                        bookmark.setDescription(descriptions.toArray()[descriptionCounter].toString());
                                    }
                                }
                                if (bookmark.getDescription() == null && bookmark.getImage() == null) {
                                    bookmark.setType(Bookmark.ItemType.SIMPLE);
                                } else if (bookmark.getDescription() == null && bookmark.getImage() != null) {
                                    bookmark.setType(Bookmark.ItemType.NO_DESCRIPTION);
                                } else if (bookmark.getDescription() != null && bookmark.getImage() == null) {
                                    bookmark.setType(Bookmark.ItemType.NO_IMAGE);
                                } else {
                                    bookmark.setType(Bookmark.ItemType.NORMAL);
                                }
                                bookmark.setLink(link);
                                bookmark.setCategory(categoryId);
                                bookmark.setReminder(ALARM_START_TIME);

                                boolean insertionResult = db.addBookmark(bookmark);
                                if (insertionResult)
                                    bookmarksCounter ++;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        descriptionCounter++;
                    }
                    runOnUiThread(() -> {
                        loadingDialog.dismissLoading();
                        Toast.makeText(BookmarksManagerActivity.this, bookmarksCounter +
                                        " segnalibri importati",
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

    ActivityResultLauncher<Intent> writeBookmarksLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                writeBookmarksFile(uri);
            }
        }
    });

    private void createBookmarksFile() {
        final String TITLE = "bookmarks-";

        CharSequence currentTime = DateFormat.format("yyyyMMddHHmm", Calendar.getInstance().getTime());
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_TITLE, TITLE + currentTime + ".html");

        writeBookmarksLauncher.launch(intent);

    }

    private void writeBookmarksFile(Uri uri) {
        final String FILE_HEADER = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<DL><p>";
        final String FILE_FOOTER = "</DL><p>";
        final String INITIAL_FILE_CONTENT = "<DT><A HREF=";
        final String FINAL_FILE_CONTENT = "</A>";
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(FILE_HEADER.getBytes());
            outputStream.write("\n".getBytes());

            for (Bookmark bookmark: bookmarks) {
                outputStream.write(INITIAL_FILE_CONTENT.getBytes());
                outputStream.write('"');
                outputStream.write(bookmark.getLink().getBytes());
                outputStream.write('"');
                outputStream.write(">".getBytes());
                if (bookmark.getDescription() != null) {
                    outputStream.write(bookmark.getDescription().getBytes());
                } else if (bookmark.getTitle() != null) {
                    outputStream.write(bookmark.getTitle().getBytes());
                } else {
                    outputStream.write(bookmark.getLink().split("//")[1].split("/")[0]
                            .getBytes());
                }
                outputStream.write(FINAL_FILE_CONTENT.getBytes());
            }
            outputStream.write(FILE_FOOTER.getBytes());
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
