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
import android.util.Log;
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
import com.ilpet.yabm.utils.PasswordDialog;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class BookmarksManagerActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final long ALARM_START_TIME = -1;
    private static final String EXPORTING_BOOKMARKS = "exporting_bookmarks";
    private static final String TEMPLATE = "template.html";
    private final Map<String, Elements> importedBookmarks = new HashMap<>();
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
                writeBookmarksFile(uri, exportManager.getBookmarksExporting());
            }
        }
    });
    ActivityResultLauncher<Intent> importBookmarksLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                getBookmarksFromUri(result.getData().getData());
                importOptionsDialog();
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
            if (isChecked) {
                if (db.getPassword() != null) {
                    exportManager.setBookmarksExporting(true);
                } else {
                    exportSwitch.setChecked(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.no_password_inserted),
                            Toast.LENGTH_LONG).show();
                }
            } else {
                exportManager.setBookmarksExporting(false);
            }
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
                ArrayList<Bookmark> bookmarks = db.getBookmarks(null, null);
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
        ArrayList<Category> categories = db.getAllCategories(null, null);
        StringBuilder stringBuilder = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(Objects.requireNonNull(inputStream)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
            Document doc = Jsoup.parse(stringBuilder.toString());
            Elements h3Tags = doc.select("h3");

            for (Element h3Tag : h3Tags) {
                String categoryTitle = h3Tag.text();
                Elements links = h3Tag.parent().select("a[href]");
                if (links != null) {
                    if (links.size() > 0) {
                        boolean categoryExists = categories.stream().anyMatch(category ->
                                category.getCategoryTitle().equals(categoryTitle));
                        if (categoryExists) {
                            importedBookmarks.put(db.getCategoryId(categoryTitle), links);
                        } else {
                            Date date = new Date();
                            Category category = new Category();
                            category.setCategoryTitle(categoryTitle);
                            category.setDate(dateFormat.format(date));
                            category.setCategoryProtection(Category.CategoryProtection.UNLOCK);
                            String categoryId = db.addCategory(category);
                            if (categoryId != null) {
                                importedBookmarks.put(categoryId, links);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), getString(R.string.something_wrong),
                    Toast.LENGTH_LONG).show();
        }
    }

    private void importOptionsDialog() {
        String question;
        Handler handler = new Handler();
//        int totalSize = 0;
//        for (List<String> list : map.values()) {
//            totalSize += list.size();
//        }

        if (importedBookmarks.entrySet().size() > 1) {
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
            importBookmarks();
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

    private void importBookmarks() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        if (importedBookmarks.size() > 0) {
            for (Map.Entry<String, Elements> entry : importedBookmarks.entrySet()) {
                for (Element link : entry.getValue()) {
                    Data data = new Data.Builder()
                            .putString("link", String.valueOf(link.attr("href")))
                            .build();

                    WorkRequest workRequest =
                            new OneTimeWorkRequest.Builder(Connection.class)
                                    .setInputData(data)
                                    .build();

                    WorkManager.getInstance(this).enqueue(workRequest);

                    WorkManager.getInstance(this).getWorkInfoByIdLiveData(workRequest.getId())
                            .observe(this, info -> {
                                if (info != null && info.getState().isFinished()) {
                                    Bookmark bookmark = new Bookmark();
                                    bookmark.setLink(String.valueOf(link.attr("href")));
                                    bookmark.setReminder(ALARM_START_TIME);
                                    bookmark.setCategory(entry.getKey());
                                    if (info.getOutputData().getString("title") != null) {
                                        bookmark.setTitle(info.getOutputData().getString("title"));
                                    } else {
                                        bookmark.setTitle(String.valueOf(link.attr("href")));
                                    }
                                    bookmark.setImage(info.getOutputData().getString("image"));
                                    bookmark.setDescription(info.getOutputData().getString("description"));
                                    String itemType = info.getOutputData().getString("itemType");
                                    if (itemType == null) {
                                        bookmark.setType(Bookmark.ItemType.SIMPLE);
                                    } else {

                                        bookmark.setType(Bookmark.ItemType.valueOf(itemType));
                                    }
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

        if (exportManager.getBookmarksExporting()) {
            PasswordDialog passwordDialog = new PasswordDialog(this,
                    result -> {
                        if (result) {
                            writeBookmarksLauncher.launch(intent);
                        }
                    });
            passwordDialog.show(getSupportFragmentManager(),
                    "Password dialog");
        } else {
            writeBookmarksLauncher.launch(intent);
        }
    }

    private void writeBookmarksFile(Uri uri, boolean exportProtected) {
        ArrayList<Bookmark> bookmarks = db.getAllBookmarks();
        StringBuilder htmlContent = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open(TEMPLATE)));

            String line;
            while ((line = reader.readLine()) != null) {
                htmlContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), (getString(R.string.something_wrong)), Toast.LENGTH_LONG)
                    .show();
        }

        Map<String, List<Bookmark>> bookmarksByCategory = new HashMap<>();
        for (Bookmark bookmark : bookmarks) {
            if (!exportProtected) {
                Category category = db.getCategoryById(bookmark.getCategory());
                if (category.getPasswordProtection() == Category.CategoryProtection.LOCK) {
                    continue;
                }
            }
            bookmarksByCategory.computeIfAbsent(bookmark.getCategory(), k -> new ArrayList<>()).add(bookmark);
        }

        int insertPosition = htmlContent.indexOf("<DL><p>") + "<DL><p>".length();

        for (Map.Entry<String, List<Bookmark>> entry : bookmarksByCategory.entrySet()) {
            String category = entry.getKey();
            List<Bookmark> bookmarksForCategory = entry.getValue();

            String categoryTitle = "\n<DT><H3>" + db.getCategoryById(category).getCategoryTitle()
                    + "</H3>\n";

            StringBuilder links = new StringBuilder();
            links.append("<DL><p>\n");
            for (Bookmark bookmark : bookmarksForCategory) {
                links.append("<DT><A HREF=\"").append(bookmark.getLink()).append("\">")
                        .append(bookmark.getTitle()).append("</A>\n");
            }
            links.append("</DL><p>\n");

            StringBuilder categoryContent = new StringBuilder();
            categoryContent.append(categoryTitle).append(links);

            htmlContent.insert(insertPosition, categoryContent);
        }

        StringBuilder dt = new StringBuilder();
        dt.append("</DL><p>");
        htmlContent.insert(htmlContent.length() - 1, dt);

        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            outputStream.write(htmlContent.toString().getBytes());
            outputStream.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), (getString(R.string.something_wrong)), Toast.LENGTH_LONG)
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
