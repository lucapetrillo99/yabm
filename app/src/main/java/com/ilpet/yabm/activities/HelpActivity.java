package com.ilpet.yabm.activities;

import android.content.res.TypedArray;
import android.os.Bundle;
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

        ViewPager2 imageContainer = findViewById(R.id.image_container);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        Bundle bundle = getIntent().getExtras();
        int help = bundle.getInt("help");

        switch (help) {
            case ADD_BOOKMARK:
                toolbarTitle.setText(R.string.add_bookmark);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.add_bookmark_images);
                break;

            case MODIFY_BOOKMARK:
                toolbarTitle.setText(R.string.modify_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.modify_bookmark_images);
                break;

            case DELETE_BOOKMARK:
                toolbarTitle.setText(R.string.delete_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.delete_bookmark_images);
                break;

            case ARCHIVE_BOOKMARK:
                toolbarTitle.setText(R.string.archive_bookmark_help);
                setSupportActionBar(toolbar);
                images = getResources().obtainTypedArray(R.array.archive_bookmark_images);
                break;
        }

        SliderAdapter adapter = new SliderAdapter(images);
        imageContainer.setAdapter(adapter);

        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, imageContainer,
                true, (tab, position) -> {
                });

        tabLayoutMediator.attach();

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}