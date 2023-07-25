package com.ilpet.yabm.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;
import com.ilpet.yabm.classes.Category;
import com.ilpet.yabm.utils.Connection;
import com.ilpet.yabm.utils.DatabaseHandler;
import com.ilpet.yabm.utils.LoadingDialog;
import com.ilpet.yabm.utils.SettingsManager;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookmarksManagerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final long ALARM_START_TIME = -1;
    private static final String EXPORTING_BOOKMARKS = "exporting_bookmarks";
    private final HashMap<String, List<String>> importedBookmarks = new HashMap<>();
    private RelativeLayout importOption;
    private RelativeLayout exportOption;
    private SwitchMaterial exportSwitch;
    private LoadingDialog loadingDialog;
    private DatabaseHandler db;
    private SettingsManager exportManager;

    ActivityResultLauncher<Intent> writeBookmarksLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                writeBookmarksFile(uri);
            }
        }
    });
    ActivityResultLauncher<Intent> importBookmarksLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                importOptionsDialog(getBookmarksFromUri(uri));
            }
        }
    });

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
        loadingDialog = new LoadingDialog(BookmarksManagerActivity.this);
        exportManager = new SettingsManager(getApplicationContext(), EXPORTING_BOOKMARKS);

        importOption = findViewById(R.id.import_option);
        exportOption = findViewById(R.id.export_option);
        exportSwitch = findViewById(R.id.export_locked_bookmarks);

        setSwitch();
        exportSwitchListener();
        importListener();
        exportListener();
        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

    }

    private void setSwitch() {
        exportSwitch.setChecked(exportManager.getBookmarksExporting());
    }

    private void exportSwitchListener() {
        exportSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            exportManager.setBookmarksExporting(isChecked);
        });
    }

    private void importListener() {
        importOption.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            PERMISSION_REQUEST_STORAGE);
                } else {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/html");
                    importBookmarksLauncher.launch(intent);
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/html");
                importBookmarksLauncher.launch(intent);
            }
        });
    }

    private void exportListener() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        } else {
            exportOption.setOnClickListener(v -> {
                ArrayList<Bookmark> bookmarks = db.getAllBookmarks(null, null);
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

    private String getBookmarksFromUri(Uri uri) {
        String categoryId = null;
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            Document doc = Jsoup.parse(stringBuilder.toString());
            Elements categoryTitles = doc.select("h3");
            if (!categoryTitles.isEmpty()) {
                if (categoryTitles.size() == 1) {
                    Elements elementsByTag = doc.getElementsByTag("a");
                    ArrayList<String> links = new ArrayList<>();
                    for (Element element : elementsByTag) {
                        links.add(element.attr("href"));
                    }
                    importedBookmarks.put(categoryTitles.get(0).text(), links);
                } else {
                    for (Element element : categoryTitles) {
                        if (element.siblingElements().select("a[href]").stream().
                                map(element1 -> element.siblingElements().select("a[href]")
                                        .attr("href")).findAny().isPresent()) {
                            importedBookmarks.put(element.text(), element.siblingElements().select("a[href]").stream().
                                    map(element1 -> element.siblingElements().select("a[href]").attr("href"))
                                    .collect(Collectors.toList()));
                        }
                    }
                }
            } else {
                Elements elementsByTag = doc.getElementsByTag("a");
                ArrayList<String> links = new ArrayList<>();
                for (Element element : elementsByTag) {
                    links.add(element.attr("href"));
                }
                importedBookmarks.put(getString(R.string.imported), links);

                String queryResult = db.getCategoryId(getString(R.string.imported));
                if (queryResult == null) {
                    Category category = new Category();
                    category.setCategoryTitle(getString(R.string.imported));
                    categoryId = db.addCategory(category);
                } else {
                    categoryId = queryResult;
                }
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG)
                    .show();
        }

        return categoryId;
    }

    private void importOptionsDialog(String categoryId) {
        String question;
        Handler handler = new Handler();
        if (importedBookmarks.size() > 1) {
            question = " segnalibri?";
        } else {
            question = " segnalibro?";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(BookmarksManagerActivity.this);
        builder.setTitle("Sei sicuro di voler importare " + importedBookmarks.values().stream()
                .mapToInt(List::size)
                .sum() + question);

        builder.setPositiveButton("Sì", (dialogInterface, i) -> {
            loadingDialog.startLoading();
            importBookmarks(categoryId);
            handler.postDelayed(() -> {
                loadingDialog.dismissLoading();
                Toast.makeText(getApplicationContext(), "Segnalibri importati", Toast.LENGTH_LONG)
                        .show();
            }, 8000);
        });
        builder.setNeutralButton("Annulla", (dialog, which) -> importedBookmarks.clear());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void importBookmarks(String categoryId) {
        if (importedBookmarks.size() > 0) {
            for (String bookmarkCategory : importedBookmarks.keySet()) {
                if (categoryId == null) {
                    Category category = new Category();
                    category.setCategoryTitle(bookmarkCategory);
                    categoryId = db.addCategory(category);
                    if (categoryId == null) {
                        categoryId = db.getCategoryId(bookmarkCategory);
                    }
                }
                for (String link : Objects.requireNonNull(importedBookmarks.get(bookmarkCategory))) {
                    Data data = new Data.Builder()
                            .putString("link", String.valueOf(link))
                            .build();

                    WorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(Connection.class)
                                    .setInputData(data)
                                    .build();

                    WorkManager.getInstance(this).enqueue(workRequest);

                    String finalCategoryId = categoryId;
                    WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                            .observe(this, info -> {
                                if (info != null && info.getState().isFinished()) {
                                    Bookmark bookmark = new Bookmark();
                                    bookmark.setLink(link);
                                    bookmark.setReminder(ALARM_START_TIME);
                                    bookmark.setCategory(finalCategoryId);
                                    bookmark.setTitle(info.getOutputData().getString("title"));
                                    bookmark.setImage(info.getOutputData().getString("image"));
                                    bookmark.setDescription(info.getOutputData().getString("description"));
                                    String itemType = info.getOutputData().getString("itemType");
                                    if (itemType == null) {
                                        bookmark.setType(Bookmark.ItemType.SIMPLE);
                                    } else {

                                        bookmark.setType(Bookmark.ItemType.valueOf(itemType));
                                    }
                                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                                            "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                    Date date = new Date();
                                    bookmark.setDate(dateFormat.format(date));
                                    db.addBookmark(bookmark);
                                }
                            });
                }
            }
        }
        importedBookmarks.clear();
    }

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
        ArrayList<Bookmark> bookmarks = db.getAllBookmarks(null, null);
        ArrayList<Category> categories = db.getAllCategories(null, null);
        final String FILE_HEADER = "<!DOCTYPE NETSCAPE-Bookmark-file-1>\n" +
                "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">\n" +
                "<TITLE>Bookmarks</TITLE>\n" +
                "<H1>Bookmarks</H1>\n" +
                "<DL><p>";
        final String FILE_FOOTER = "</DL><p>";
        final String INITIAL_FILE_CONTENT = "<DT><A HREF=";
        final String FINAL_FILE_CONTENT = "</A>";
        bookmarks.sort(Comparator.comparingInt(bookmark -> Integer.parseInt(bookmark.getCategory())));
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(FILE_HEADER.getBytes());
            outputStream.write("\n".getBytes());

            String bookmarkCategoryId = null;

            for (Bookmark bookmark : bookmarks) {
                if (bookmarkCategoryId == null) {
                    bookmarkCategoryId = writeObject(categories, outputStream, bookmark);
                } else {
                    if (bookmarkCategoryId.equals(bookmark.getCategory())) {
                        outputStream.write(INITIAL_FILE_CONTENT.getBytes());
                        outputStream.write('"');
                        outputStream.write(bookmark.getLink().getBytes());
                        outputStream.write('"');
                        outputStream.write(">".getBytes());
                        outputStream.write(bookmark.getTitle().getBytes());
                        outputStream.write(FINAL_FILE_CONTENT.getBytes());
                    } else {
                        bookmarkCategoryId = writeObject(categories, outputStream, bookmark);
                    }
                }
            }
            outputStream.write(FILE_FOOTER.getBytes());
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Qualcosa è andato storto", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private String writeObject(ArrayList<Category> categories, OutputStream outputStream, Bookmark bookmark) throws IOException {
        String bookmarkCategoryId = writeAndGetCategoryId(categories, outputStream, bookmark);
        outputStream.write("<DT><A HREF=".getBytes());
        outputStream.write('"');
        outputStream.write(bookmark.getLink().getBytes());
        outputStream.write('"');
        outputStream.write(">".getBytes());
        outputStream.write(bookmark.getTitle().getBytes());
        outputStream.write("</A>".getBytes());
        return bookmarkCategoryId;
    }

    private String writeAndGetCategoryId(ArrayList<Category> categories, OutputStream outputStream, Bookmark bookmark) throws IOException {
        Category category = categories.stream()
                .filter(category1 -> bookmark.getCategory().equals(category1.getCategoryId()))
                .findAny()
                .orElse(null);
        String categoryId = bookmark.getCategory();
        outputStream.write("<DT><H3>".getBytes());
        if (category != null) {
            outputStream.write(category.getCategoryTitle().getBytes());
            outputStream.write("</H3>".getBytes());
        }
        return categoryId;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
