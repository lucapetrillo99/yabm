package com.ilpet.yabm.activities;

import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.Html;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.ilpet.yabm.R;
import com.ilpet.yabm.adapters.SliderAdapter;


public class HelpActivity extends AppCompatActivity {
    private TypedArray images;
    private TextView[] dots;
    private LinearLayout layout;
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
        layout = findViewById(R.id.dots_container);

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

        dots = new TextView[images.length()];

        SliderAdapter adapter = new SliderAdapter(images);
        imageContainer.setAdapter(adapter);
        setIndicators();

        imageContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectedDots(position);
                super.onPageSelected(position);
            }
        });

        toolbar.setNavigationOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });
    }

    private void selectedDots(int position) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getTheme();
        theme.resolveAttribute(R.attr.strokeColor, typedValue, true);
        @ColorInt int color = typedValue.data;

        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setTextColor(getResources().getColor(R.color.orange));
            } else {
                dots[i].setTextColor(color);
            }
        }
    }

    private void setIndicators() {
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#9679;"));
            dots[i].setTextSize(18);
            layout.setWeightSum(dots.length);
            layout.addView(dots[i]);
        }
    }
}