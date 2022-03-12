package com.ilpet.yabm.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ilpet.yabm.utils.AlarmReceiver;
import com.ilpet.yabm.utils.BackupHandler;
import com.ilpet.yabm.R;
import com.ilpet.yabm.utils.SettingsManager;
import com.ilpet.yabm.utils.StoragePermissionDialog;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.File;
import java.io.FileWriter;
import java.util.Calendar;

public class BackupActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_STORAGE = 1000;
    private static final int RESULT_OK = 1;
    private static final String AUTO_BACKUP = "auto_backup";
    private static final String AUTO_BACKUP_URI = "auto_backup_uri";
    private static final String FILE_EXTENSION = "db";
    private static final String DATE_PATTERN = "yyyyMMddHHmmss";
    private SwitchMaterial autoBackupSwitch;
    private RelativeLayout createBackupOption;
    private RelativeLayout restoreBackupOption;
    private BackupHandler backupHandler;
    private SettingsManager autoBackupManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.backup_title);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        backupHandler = BackupHandler.getInstance(getApplicationContext());
        autoBackupManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP);

        autoBackupSwitch = findViewById(R.id.auto_backup_switch);
        createBackupOption = findViewById(R.id.create_backup_option);
        restoreBackupOption = findViewById(R.id.restore_backup_option);

        setAutoBackupSwitch();
        autoBackupListener();
        createBackupListener();
        restoreBackupListener();

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void setAutoBackupSwitch() {
        autoBackupSwitch.setChecked(autoBackupManager.getAutoBackup());
    }

    private void autoBackupListener() {
        autoBackupSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                createAutoBackupFile();
                autoBackupManager.setAutoBackup(true);
            } else {
                cancelAutoBackup();
                autoBackupManager.setAutoBackup(false);
            }
        });
    }

    ActivityResultLauncher<Intent> createBackupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                int backupResult = backupHandler.createBackup(uri);
                if (backupResult == RESULT_OK) {
                    Toast.makeText(getApplicationContext(), "Backup creato correttamente",
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Impossibile creare il backup",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    });

    private void createBackupListener() {
        createBackupOption.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {
                final String TITLE = "bookmarks-";

                CharSequence currentTime = DateFormat.format(DATE_PATTERN, Calendar.getInstance().getTime());
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("text/*");
                intent.putExtra(Intent.EXTRA_TITLE, TITLE + currentTime + ".db");

                createBackupLauncher.launch(intent);
            }
        });
    }

    ActivityResultLauncher<Intent> restoreBackupLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == Activity.RESULT_OK) {
            if (result.getData() != null) {
                Uri uri = result.getData().getData();
                if (checkFileExtension(uri)) {
                    int backupResult = backupHandler.restoreBackup(uri);
                    if (backupResult == RESULT_OK) {
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
        }
    });

    private void restoreBackupListener() {
        restoreBackupOption.setOnClickListener(v -> {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
            } else {

                RelativeLayout autoRestore, manualRestore;
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = getLayoutInflater();
                View view = inflater.inflate(R.layout.restore_backup_dialog, null);
                builder.setView(view)
                        .setTitle("Scegli l'operazione")
                        .setNegativeButton("Annulla", (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                autoRestore = view.findViewById(R.id.auto_restore);
                manualRestore = view.findViewById(R.id.manual_restore);

                autoRestore.setOnClickListener(a -> {
                    SettingsManager settingsManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP_URI);
                    if (settingsManager.getAutoBackupUri() != null) {
                        File file = new File(settingsManager.getAutoBackupUri());
                        Uri uri = Uri.fromFile(file);
                        int result = backupHandler.restoreBackup(uri);
                        if (result == RESULT_OK) {
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Backup ripristinato correttamente",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            alertDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Impossibile ripristinare il backup",
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        alertDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Non sono presenti backup da ripristinare",
                                Toast.LENGTH_LONG).show();
                    }

                });

                manualRestore.setOnClickListener(m -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("*/*");

                    restoreBackupLauncher.launch(intent);
                    alertDialog.dismiss();
                });
            }
        });
    }

    private boolean checkFileExtension(Uri uri) {
        Cursor returnCursor =
                getContentResolver().query(uri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);
        String fileExtension = fileName.split("\\.")[1];
        returnCursor.close();
        return fileExtension.equals(FILE_EXTENSION);
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

    private void createAutoBackupFile() {
        File dir = new File(getFilesDir() + File.separator +
                getString(R.string.app_name));
        if (!dir.exists()) {
            dir.mkdir();
        }
        try {
            CharSequence currentTime = DateFormat.format("yyyyMMdd", Calendar.getInstance().getTime());
            File backupFile = new File(dir, "bookmark-backup" + currentTime + ".db");
            FileWriter writer = new FileWriter(backupFile);
            writer.flush();
            writer.close();
            Uri uri = Uri.fromFile(backupFile);
            int result = backupHandler.createBackup(uri);
            if (result == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Backup creato correttamente",
                        Toast.LENGTH_LONG).show();
                scheduleAutoBackup(uri);
                SettingsManager settingsManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP_URI);
                settingsManager.setAutoBackupUri(backupFile.getPath());
            } else {
                Toast.makeText(getApplicationContext(), "Impossibile creare il backup",
                        Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Impossibile impostare il backup automatico",
                    Toast.LENGTH_LONG).show();
        }
    }

    private void scheduleAutoBackup(Uri uri) {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        intent.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent,
                PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 2);

        if (alarmManager != null) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    private void cancelAutoBackup() {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
        SettingsManager settingsManager = new SettingsManager(getApplicationContext(), AUTO_BACKUP_URI);
        File file = new File(settingsManager.getAutoBackupUri());
        Uri uri = Uri.fromFile(file);
        intent.setData(uri);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

