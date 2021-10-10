package com.ilpet.yabm.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.ilpet.yabm.R;
import com.ilpet.yabm.utils.SettingsManager;

public class SplashScreenActivity extends AppCompatActivity {
    private static final int SYSTEM_DEFAULT = 0;
    private static final int LIGHT_MODE = 1;
    private static final int NIGHT_MODE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppTheme();
        startActivity(new Intent(this, MainActivity.class));
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        finish();
    }

    public void setAppTheme() {
        SettingsManager settingsManager = new SettingsManager(this, "theme");
        int theme = settingsManager.getTheme();
        switch (theme) {
            case NIGHT_MODE:
                settingsManager.setTheme(NIGHT_MODE);
                break;
            case LIGHT_MODE:
                settingsManager.setTheme(LIGHT_MODE);
                break;
            default:
                settingsManager.setTheme(SYSTEM_DEFAULT);
        }
    }
}