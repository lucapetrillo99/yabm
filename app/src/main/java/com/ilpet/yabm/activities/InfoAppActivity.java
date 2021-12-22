package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.TextView;

import com.ilpet.yabm.R;

public class InfoAppActivity extends AppCompatActivity {
    private static final String RIGHTS_RESERVED = "Yabm e i suoi loghi sono marchi riservati.\n" +
            "Tutti i diritti sono riservati.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_app);
        TextView appVersion = findViewById(R.id.app_version);
        TextView rightsReserved = findViewById(R.id.rights_reserved);
        SpannableString spannableString = new SpannableString(RIGHTS_RESERVED);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        rightsReserved.setText(spannableString);

        try {
            PackageInfo packageInfo = getApplicationContext().getPackageManager().
                    getPackageInfo(getApplicationContext().getPackageName(), 0);
            appVersion.setText(getString(R.string.version, packageInfo.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}