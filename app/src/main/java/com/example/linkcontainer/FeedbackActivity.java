package com.example.linkcontainer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FeedbackActivity extends AppCompatActivity {
    private EditText subject;
    private EditText text;
    private static final String MAIL = "lucapetrillo0@gmail.com";

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText("Invia feedback");
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        subject = findViewById(R.id.title_mail);
        text = findViewById(R.id.text_mail);

        toolbar.setNavigationOnClickListener(v -> {
            if (subject.getText().toString().isEmpty() && text.getText().toString().isEmpty()) {
                finish();

            } else {
                confirmDialog();
            }
        });

        FloatingActionButton buttonSend = findViewById(R.id.send_mail);

        buttonSend.setOnClickListener(v -> {
            String textToSend = text.getText().toString();
            if (textToSend.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Scrivi qualcosa per darci maggiori " +
                        "informazioni", Toast.LENGTH_LONG).show();
            } else {
                sendMail();
            }
        });
    }

    private void sendMail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{MAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject.getText().toString());
        intent.putExtra(Intent.EXTRA_TEXT, text.getText().toString());
        startActivity(Intent.createChooser(intent, "Scegli una applicazione"));
    }

    private void confirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Sei sicuro di voler uscire?\nTutti i cambiamenti andranno persi")
                .setCancelable(false)
                .setNegativeButton("No", (dialogInterface, i) -> dialogInterface.cancel())
                .setPositiveButton("Sì", (dialogInterface, i) -> finish());
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}