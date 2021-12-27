package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.adapters.SliderAdapter;

import java.io.IOException;
import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {
    private ViewPager2 imageContainer;
    private SliderAdapter adapter;
    private ArrayList<Drawable> list;
    private TextView[] dots;
    private LinearLayout layout;
    private static final int ADD_BOOKMARK = 1;
    private static final int Q = 2;
    private static final int RE = 3;
    private static final int T = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        imageContainer = findViewById(R.id.image_container);
        layout = findViewById(R.id.dots_container);

        Bundle helpType = getIntent().getExtras();
        int help = helpType.getInt("help");

        switch (help) {
            case ADD_BOOKMARK:
                list = new ArrayList<>();
                list = getImages();
        }

        dots = new TextView[5];

//
//        list[0] = getResources().getColor(R.color.black);
//        list[1] = getResources().getColor(R.color.red);
//        list[2] = getResources().getColor(R.color.light_black);
//        list[3] = getResources().getColor(R.color.dirty_white);
//        list[4] = getResources().getColor(R.color.orange);

        adapter = new SliderAdapter(list);
        imageContainer.setAdapter(adapter);

        setIndicators();

        imageContainer.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                selectedDots(position);
                super.onPageSelected(position);
            }
        });

    }

    private void selectedDots(int position) {
        for (int i = 0; i < dots.length; i++) {
            if (i == position) {
                dots[i].setTextColor(getResources().getColor(R.color.red));
            } else {
                dots[i].setTextColor(getResources().getColor(R.color.gray));
            }
        }
    }

    private void setIndicators() {
        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#9679;"));
            dots[i].setTextSize(18);
            layout.addView(dots[i]);
        }

    }

    private ArrayList<Drawable> getImages()  {
        final TypedArray imgs = getResources().obtainTypedArray(R.array.add_bookmark_images);
        ArrayList<Drawable> images = new ArrayList<>();
        for (int i = 0; i < imgs.length(); i ++) {
            images.add(imgs.getDrawable(i));
        }
        return images;
    }
}