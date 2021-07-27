package com.example.linkcontainer;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

public class StoragePermissionDialog {
    private final Activity activity;

    StoragePermissionDialog(Activity activity) {
        this.activity = activity;
    }

    public void showWarningMessage() {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), "",
                Snackbar.LENGTH_LONG);

        View customView = View.inflate(activity, R.layout.storage_warning_dialog, null);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0, 0, 0, 0);

        customView.findViewById(R.id.go_to_settings).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
            intent.setData(uri);
            activity.startActivity(intent);
        });

        customView.findViewById(R.id.warning_close_button).setOnClickListener(v -> snackbar.dismiss());
        snackbarLayout.addView(customView, 0);
        snackbar.show();

    }
}
