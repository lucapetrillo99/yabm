package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.widget.ImageView;
import android.widget.TextView;

import com.ilpet.yabm.R;
import com.ilpet.yabm.utils.SettingsManager;

public class InfoAppActivity extends AppCompatActivity {
    private static final String THEME = "theme";
    private static final int SYSTEM_DEFAULT = 0;
    private static final int LIGHT_MODE = 1;
    private static final int NIGHT_MODE = 2;
    private static final String RIGHTS_RESERVED = "Yabm e i suoi loghi sono marchi riservati.\n" +
            "Tutti i diritti sono riservati.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_info_app);
        TextView appVersion = findViewById(R.id.app_version);
        ImageView appName = findViewById(R.id.app_name);
        ImageView appLogo = findViewById(R.id.app_logo);
        TextView rightsReserved = findViewById(R.id.rights_reserved);
        SpannableString spannableString = new SpannableString(RIGHTS_RESERVED);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        rightsReserved.setText(spannableString);


        SettingsManager themeManager = new SettingsManager(getApplicationContext(), THEME);
        switch (themeManager.getTheme()) {

            case NIGHT_MODE:
                appLogo.setImageResource(R.drawable.ic_app_logo_round_black);
                appName.setImageResource(R.drawable.name_logo_white_160);
                break;
            case LIGHT_MODE:
                appLogo.setImageResource(R.drawable.ic_app_logo_round);
                appName.setImageResource(R.drawable.name_logo_black_160);
                break;
            case SYSTEM_DEFAULT:
                int currentNightMode = getApplicationContext().getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK;
                switch (currentNightMode) {
                    case Configuration.UI_MODE_NIGHT_NO:
                        appLogo.setImageResource(R.drawable.ic_app_logo_round);
                        appName.setImageResource(R.drawable.name_logo_black_160);
                        break;
                    case Configuration.UI_MODE_NIGHT_YES:
                        appLogo.setImageResource(R.drawable.ic_app_logo_round_black);
                        appName.setImageResource(R.drawable.name_logo_white_160);
                        break;
                }
                break;
        }

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