package com.ilpet.yabm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.ilpet.yabm.R;

import java.util.Objects;

public class PasswordManagerDialog extends AppCompatDialogFragment {
    private final Activity activity;
    private final PasswordManagerDialog.PasswordManagerListener passwordManagerListener;

    public PasswordManagerDialog(Activity activity, PasswordManagerListener passwordListener) {
        this.activity = activity;
        this.passwordManagerListener = passwordListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        DatabaseHandler db = new DatabaseHandler(activity.getApplicationContext());
        String currentPassword = db.getPassword();
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.password_manager_dialog, null);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(activity.getString(R.string.cancel), null)
                .create();
        dialog.setCanceledOnTouchOutside(false);

        TextInputLayout userPasswordLayout = dialogView.findViewById(R.id.current_password_layout);
        EditText passwordText = dialogView.findViewById(R.id.password);
        EditText confirmPasswordText = dialogView.findViewById(R.id.password_confirmation);
        EditText userPassword = dialogView.findViewById(R.id.current_password);
        TextView title = dialogView.findViewById(R.id.password_title);
        ImageButton info = dialogView.findViewById(R.id.info_button);
        if (currentPassword != null) {
            title.setText(activity.getString(R.string.change_password));
            userPasswordLayout.setVisibility(View.VISIBLE);
        } else {
            title.setText(activity.getString(R.string.add_password));
            userPasswordLayout.setVisibility(View.GONE);
        }

        info.setOnClickListener(view -> {
            LayoutInflater popupInflater = activity.getLayoutInflater();
            View dialogView1 = popupInflater.inflate(R.layout.info_password_popup, null);
            AlertDialog dialog1 = new AlertDialog.Builder(activity)
                    .setView(dialogView1)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
            dialog1.show();
        });

        dialog.setOnShowListener(dialogInterface -> {
            Button button = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String password = Objects.requireNonNull(passwordText.getText()).toString();
                String confirmedPassword = confirmPasswordText.getText().toString();
                if (currentPassword != null) {
                    String oldPassword = userPassword.getText().toString();
                    if (oldPassword.isEmpty() || password.isEmpty() || confirmedPassword.isEmpty()) {
                        Toast.makeText(activity,
                                        activity.getString(R.string.empty_fields), Toast.LENGTH_LONG).
                                show();
                        passwordText.getText().clear();
                        confirmPasswordText.getText().clear();
                    } else {
                        PasswordManager passwordManager = PasswordManager.getInstance();
                        if (passwordManager.verifyPassword(oldPassword, currentPassword)) {
                            if (password.equals(confirmedPassword)) {
                                if (!password.equals(oldPassword)) {
                                    if (db.updatePassword(password)) {
                                        Toast.makeText(activity,
                                                activity.getString(R.string.password_updated),
                                                Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(activity, activity.getString(R.string.
                                                impossible_update_password), Toast.LENGTH_LONG).show();
                                    }
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(activity,
                                            activity.getString(R.string.same_previous_password),
                                            Toast.LENGTH_LONG).show();
                                    userPassword.getText().clear();
                                    passwordText.getText().clear();
                                    confirmPasswordText.getText().clear();
                                }
                            } else {
                                Toast.makeText(activity,
                                        activity.getString(R.string.passwords_not_match),
                                        Toast.LENGTH_LONG).show();
                                userPassword.getText().clear();
                                passwordText.getText().clear();
                                confirmPasswordText.getText().clear();
                            }
                        } else {
                            Toast.makeText(activity,
                                    activity.getString(R.string.wrong_password),
                                    Toast.LENGTH_LONG).show();
                            userPassword.getText().clear();
                            passwordText.getText().clear();
                            confirmPasswordText.getText().clear();
                        }

                    }
                } else {
                    if (password.isEmpty() || confirmedPassword.isEmpty()) {
                        Toast.makeText(activity,
                                        activity.getString(R.string.empty_fields), Toast.LENGTH_LONG).
                                show();
                        passwordText.getText().clear();
                        confirmPasswordText.getText().clear();
                    } else {
                        if (password.equals(confirmedPassword)) {
                            if (db.insertPassword(password)) {
                                passwordManagerListener.getResult(true);
                                Toast.makeText(activity, activity.getString(R.string.password_added),
                                        Toast.LENGTH_LONG).show();
                            } else {
                                passwordManagerListener.getResult(false);
                                Toast.makeText(activity,
                                        activity.getString(R.string.impossible_add_password),
                                        Toast.LENGTH_LONG).show();
                            }
                            dialog.dismiss();
                        } else {
                            Toast.makeText(activity,
                                    activity.getString(R.string.passwords_not_match),
                                    Toast.LENGTH_LONG).show();
                            passwordText.getText().clear();
                            confirmPasswordText.getText().clear();
                        }
                    }
                }
            });
        });
        return dialog;
    }

    public interface PasswordManagerListener {
        void getResult(boolean result);
    }
}
