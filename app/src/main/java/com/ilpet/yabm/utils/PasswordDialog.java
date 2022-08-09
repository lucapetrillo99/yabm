package com.ilpet.yabm.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
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
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        final View customLayout = getLayoutInflater().inflate(R.layout.password_dialog, null);
        builder.setView(customLayout);

        builder.setPositiveButton(activity.getString(R.string.ok), (dialog, which) -> {
            EditText input = customLayout.findViewById(R.id.password_input);
            String userInput = input.getText().toString();
            if (!userInput.isEmpty()) {
                passwordListener.getResult(currentPassword.equals(userInput));
            } else {
                Toast.makeText(activity, getString(R.string.empty_fields),
                        Toast.LENGTH_LONG).show();
            }
        });

        return builder.create();
    }

    public interface PasswordListener {
        void getResult(boolean result);
    }
}
