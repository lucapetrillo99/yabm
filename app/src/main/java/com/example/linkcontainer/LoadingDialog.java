package com.example.linkcontainer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;

public class LoadingDialog {

    Activity activity;
    AlertDialog dialog;

    LoadingDialog (Activity activity) {
        this.activity = activity;

    }

    @SuppressLint("InflateParams")
    public void startLoading() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.loading_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.show();
    }

    public void dismissLoading() {
        dialog.dismiss();
    }
}
