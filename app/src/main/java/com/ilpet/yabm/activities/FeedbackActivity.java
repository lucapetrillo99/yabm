package com.ilpet.yabm.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.ilpet.yabm.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FeedbackActivity extends AppCompatActivity {
    private EditText subject;
    private EditText text;
    private static final String MAIL = "lucapetrillo0@gmail.com";
    private String finalInformation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.feedback_title);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        subject = findViewById(R.id.title_mail);
        text = findViewById(R.id.text_mail);

        toolbar.setNavigationOnClickListener(v -> {
            if (subject.getText().toString().isEmpty() && text.getText().toString().isEmpty()) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                confirmDialog();
            }
        });

        FloatingActionButton buttonSend = findViewById(R.id.send_mail);

        buttonSend.setOnClickListener(v -> {
            String textToSend = text.getText().toString();
            if (textToSend.isEmpty()) {
                Toast.makeText(getApplicationContext(), getString(R.string.give_us_information),
                        Toast.LENGTH_LONG).show();
            } else {
                sendMail();
            }
        });
    }

    private void sendMail() {
        getDeviceInformation();
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, text.getText().toString() + "\n\n\n" + finalInformation);
        startActivity(Intent.createChooser(intent, getString(R.string.choose_an_application)));
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.exit_question))
                .setCancelable(false)
                .setNegativeButton(getString(R.string.cancel), (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> {
                    finish();
                    overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void getDeviceInformation() {
        String brand = Build.BRAND;
        String model = Build.MODEL;
        int sdk = Build.VERSION.SDK_INT;
        String product = Build.PRODUCT;
        String version = Build.VERSION.RELEASE;

        finalInformation = brand + " " + model + " (" + product + ")" + " Android " + version + " "
                + sdk;
    }

    @Override
    public void onBackPressed() {
        if (subject.getText().toString().isEmpty() && text.getText().toString().isEmpty()) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else {
            confirmDialog();
        }
    }
}