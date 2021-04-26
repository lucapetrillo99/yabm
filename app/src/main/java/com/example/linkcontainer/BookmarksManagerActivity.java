package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Objects;

import at.blogc.android.views.ExpandableTextView;

public class BookmarksManagerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 10;
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private RelativeLayout importOption;
    private ExpandableTextView importOptionText;
    private TextView description;
    private Button selectButton;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks_manager);

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
                intent.setType("text/*");

                startActivityForResult(intent, PERMISSION_REQUEST_STORAGE);

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.i("IOFHDO", "ENTRO RESULT");
        Log.i("IOFHDO", "CODE MIO" + REQUEST_CODE + " RESULT come dovrebbe " + Activity.RESULT_OK);
        Log.i("IOFHDO", "CODE " + requestCode + " RESULT " + resultCode);
        if (requestCode == PERMISSION_REQUEST_STORAGE && resultCode == Activity.RESULT_OK) {
            Log.i("IOFHDO", "ENTRO IF");
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri;
            Log.i("IOFHDO", String.valueOf(data != null));
            if (data != null) {
                uri = data.getData();
                readTextFromUri(uri);
            }

            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showMessage();
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                startActivityForResult(intent, REQUEST_CODE);
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

    private void readTextFromUri(Uri uri) {
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
        Elements links = document.select("a[href]");
        for (Element element : links) {
            
        }

    }

}