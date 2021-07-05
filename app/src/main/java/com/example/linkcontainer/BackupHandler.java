package com.example.linkcontainer;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class BackupHandler {
    private static final int RESULT_OK = 1;
    private static final int ERROR = -1;
    private static BackupHandler instance;
    private final DatabaseHandler db;
    private final Context context;

    public static synchronized BackupHandler getInstance(Context context) {
        if (instance == null)
            instance = new BackupHandler(context.getApplicationContext());

        return instance;
    }

    public BackupHandler(Context context) {
       this.context = context;
       this.db = DatabaseHandler.getInstance(context);
    }

    public int createBackup(Uri uri) {
        final String inFileName = db.getDbPath(context);
        try {
            File dbFile = new File(inFileName);
            FileInputStream fileInputStream = new FileInputStream(dbFile);

            OutputStream output = context.getContentResolver().openOutputStream(uri);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fileInputStream.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fileInputStream.close();

            return RESULT_OK;

        } catch (Exception e) {
            return ERROR;
        }
    }

    public int restoreBackup(Uri uri) {
        final String outFileName = db.getDbPath(context);

        try {

            InputStream fis = context.getContentResolver().openInputStream(uri);

            OutputStream output = new FileOutputStream(outFileName);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                output.write(buffer, 0, length);
            }

            output.flush();
            output.close();
            fis.close();

            return RESULT_OK;

        } catch (Exception e) {
            return ERROR;
        }
    }
}
