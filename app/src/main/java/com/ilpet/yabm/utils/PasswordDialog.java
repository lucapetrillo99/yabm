package com.ilpet.yabm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.ilpet.yabm.R;

public class PasswordDialog extends AppCompatDialogFragment {
    private final Activity activity;
    private final PasswordListener passwordListener;

    public PasswordDialog(Activity activity, PasswordListener passwordListener) {
        this.activity = activity;
        this.passwordListener = passwordListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatabaseHandler db = new DatabaseHandler(activity.getApplicationContext());
        String currentPassword = db.getPassword();
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.password_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                EditText input = dialogView.findViewById(R.id.password_input);
                String userInput = input.getText().toString();
                if (!userInput.isEmpty()) {
                    if (currentPassword.equals(userInput)) {
                        passwordListener.getResult(true);
                        dialog.dismiss();
                    } else {
                        passwordListener.getResult(false);
                        Toast.makeText(activity, getString(R.string.wrong_password),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    passwordListener.getResult(false);
                    Toast.makeText(activity, getString(R.string.empty_fields),
                            Toast.LENGTH_LONG).show();
                }
            });
        });

        return dialog;
    }

    public interface PasswordListener {
        void getResult(boolean result);
    }
}
