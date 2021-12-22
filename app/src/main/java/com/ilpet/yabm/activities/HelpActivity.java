package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.adapters.SliderAdapter;

public class HelpActivity extends AppCompatActivity {
    private LinearLayout dotsLayout;
    private SliderAdapter sliderAdapter;
    ViewPager2 viewPager2;
    private int list[];
    private TextView[] dots;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        list = new int[5];
        list[0] = getResources().getColor(R.color.black);
        list[1] = getResources().getColor(R.color.light_black);
        list[2] = getResources().getColor(R.color.dirty_black);
        list[3] = getResources().getColor(R.color.dirty_white);
        list[4] = getResources().getColor(R.color.orange);

        sliderAdapter = new SliderAdapter(list);
        viewPager2.setAdapter(sliderAdapter);

        dots = new TextView[5];
        dotsIndicator();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                selectedIndicator(position);
            }
        });
    }

    private void selectedIndicator(int position) {
        for (int i = 0; i < dots.length; i ++) {
            if (i == position) {
                dots[i].setTextColor(list[position]);
            } else {
                dots[i].setTextColor(getResources().getColor(R.color.red));
            }
        }
    }

    private void dotsIndicator() {
        for (int i = 0; i < dots.length; i ++) {
            dots[i] = new TextView((this));
            dots[i].setText(Html.fromHtml("#00a2f5"));
            dots[i].setTextSize(18);
            dotsLayout.addView(dots[i]);
        }
    }
}