package com.example.linkcontainer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Calendar;

public class BackupActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int RESULT_OK = 1;
    private static final int REQUEST_CODE = 1;
    private static final String AUTO_BACKUP = "auto_backup";
    private static final String FILE_EXTENSION = "db";
    private SwitchMaterial autoBackupSwitch;
    private RelativeLayout createBackupOption;
    private RelativeLayout restoreBackupOption;
    private BackupHandler backupHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Backup");
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        backupHandler = BackupHandler.getInstance(getApplicationContext());

        autoBackupSwitch = findViewById(R.id.auto_backup_switch);
        createBackupOption = findViewById(R.id.create_backup_option);
        restoreBackupOption = findViewById(R.id.restore_backup_option);

        setAutoBackupSwitch();
        autoBackupListener();
        createBackupListener();
        restoreBackupListener();

        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setAutoBackupSwitch() {
        SettingsManager autoBackupManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP);
        boolean autoBackup = autoBackupManager.getAutoBackup();

        autoBackupSwitch.setChecked(autoBackup);
    }

    private void autoBackupListener() {
        SettingsManager autoBackupManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP);
        autoBackupSwitch.setOnClickListener(v -> autoBackupManager.setAutoBackup(autoBackupSwitch.isChecked()));
    }

    private void createBackupListener() {
        createBackupOption.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                final String TITLE = "bookmarks-";

                CharSequence currentTime = DateFormat.format("yyyyMMddHHmmss", Calendar.getInstance().getTime());
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_TITLE, TITLE + currentTime + ".db");

                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }

    private void restoreBackupListener() {
        restoreBackupOption.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");

                startActivityForResult(intent, PERMISSION_REQUEST_STORAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PERMISSION_REQUEST_STORAGE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (checkFileExtension(uri)) {
                    int result = backupHandler.restoreBackup(uri);
                    if (result == RESULT_OK) {
                        Toast.makeText(getApplicationContext(), "Backup ripristinato correttamente",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Impossibile ripristinare il backup",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "File selezionato non valido",
                            Toast.LENGTH_LONG).show();
                }
            }
        } else if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                int result = backupHandler.createBackup(uri);
                if (result == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Backup creato correttamente",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Impossibile creare il backup",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private boolean checkFileExtension(Uri uri) {
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);
        String fileExtension = fileName.split("\\.")[1];
        return fileExtension.equals(FILE_EXTENSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                showWarningMessage();
            }
        }
    }

    private void showWarningMessage() {
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
}

