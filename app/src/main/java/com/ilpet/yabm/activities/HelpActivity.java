package com.ilpet.yabm.activities;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.ilpet.yabm.R;
import com.ilpet.yabm.adapters.SliderAdapter;

public class HelpActivity extends AppCompatActivity {
    private TypedArray images;
    private TypedArray info;
    private ViewPager2 imageContainer;
    private Button next, previous;
    private int currentPosition;
    private boolean skip = false;
    private boolean finished = false;
    private static final int ADD_BOOKMARK = 1;
    private static final int MODIFY_BOOKMARK = 2;
    private static final int DELETE_BOOKMARK = 3;
    private static final int ARCHIVE_BOOKMARK = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbar.setNavigationIcon(R.drawable.ic_back_button);

        imageContainer = findViewById(R.id.image_container);
        TabLayout tabLayout = findViewById(R.id.tab_layout);
        next = findViewById(R.id.help_next);
        previous = findViewById(R.id.help_previous);

        Bundle bundle = getIntent().getExtras();
        int help = bundle.getInt("help");

        switch (help) {
            case ADD_BOOKMARK:
                toolbarTitle.setText(R.string.add_bookmark);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.add_bookmark_images);
                info = getResources().obtainTypedArray(R.array.add_bookmark_info);
                break;

            case MODIFY_BOOKMARK:
                toolbarTitle.setText(R.string.modify_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.modify_bookmark_images);
                info = getResources().obtainTypedArray(R.array.modify_bookmark_info);
                break;

            case DELETE_BOOKMARK:
                toolbarTitle.setText(R.string.delete_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.delete_bookmark_images);
                info = getResources().obtainTypedArray(R.array.delete_bookmark_info);
                break;

            case ARCHIVE_BOOKMARK:
                toolbarTitle.setText(R.string.archive_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.archive_bookmark_images);
                info = getResources().obtainTypedArray(R.array.archive_bookmark_info);
                break;
        }

        SliderAdapter adapter = new SliderAdapter(images, info);
        imageContainer.setAdapter(adapter);

        imageContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                if (position == 0) {
                    previous.setText(getText(R.string.skip));
                    next.setText(getText(R.string.next));
                    skip = true;
                    finished = false;
                } else if (position == adapter.getItemCount() - 1) {
                    next.setText(getText(R.string.finished));
                    finished = true;
                } else {
                    previous.setText(getText(R.string.retry));
                    next.setText(getText(R.string.next));
                    skip = false;
                    finished = false;
                }
            }
        });

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, imageContainer,
                true, (tab, position) -> {
        });

        tabLayoutMediator.attach();

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        nextClickListener();
        previousClickListener();
    }

    private void nextClickListener() {
        next.setOnClickListener(v -> {
            if (finished) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                imageContainer.setCurrentItem(currentPosition + 1);
            }
        });
    }

    private void previousClickListener() {
        previous.setOnClickListener(v -> {
            if (skip) {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            } else {
                imageContainer.setCurrentItem(currentPosition - 1);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}