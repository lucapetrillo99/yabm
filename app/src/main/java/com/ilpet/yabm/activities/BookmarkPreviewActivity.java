package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.classes.Bookmark;

public class BookmarkPreviewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark_preview);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(R.string.preview);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);
        TextView link = findViewById(R.id.link);
        TextView title = findViewById(R.id.title);
        TextView categoryField = findViewById(R.id.category);
        TextView descriptionTitle = findViewById(R.id.description_title);
        TextView description = findViewById(R.id.description);
        TextView reminderTitle = findViewById(R.id.reminder_title);
        TextView reminder = findViewById(R.id.inserted_date);


        Intent intent = getIntent();

        if (intent.getExtras() != null) {
            if (intent.getSerializableExtra("bookmark") != null) {
                Bookmark bookmark = (Bookmark) intent.getSerializableExtra("bookmark");
                String category = intent.getStringExtra("category");
                link.setText(bookmark.getLink());
                title.setText(bookmark.getTitle());
                categoryField.setText(category);
                if (bookmark.getDescription() != null) {
                    descriptionTitle.setVisibility(View.VISIBLE);
                    description.setVisibility(View.VISIBLE);
                    description.setText(bookmark.getDescription());
                } else {
                    descriptionTitle.setVisibility(View.GONE);
                    description.setVisibility(View.GONE);
                }

                if (bookmark.getReminder() != -1) {
                    reminderTitle.setVisibility(View.VISIBLE);
                    reminder.setVisibility(View.VISIBLE);
                    reminder.setText(DateFormat.format("dd/MM/yyyy HH:mm", bookmark.getReminder()));
                } else {
                    reminderTitle.setVisibility(View.GONE);
                    reminder.setVisibility(View.GONE);
                }

            }
        }

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }
}