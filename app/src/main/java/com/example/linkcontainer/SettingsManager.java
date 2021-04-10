package com.example.linkcontainer;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private final SharedPreferences settingsManager;
    private static final String THEME = "theme";
    private static final String CATEGORY = "category";
    private static final int SYSTEM_DEFAULT = 0;
    private static final int LIGHT_MODE = 1;
    private static final int NIGHT_MODE = 2;
    private static final int ALL_CATEGORIES = 0;

    public SettingsManager(Context context, String setting) {
        this.settingsManager = context.getSharedPreferences(setting, Context.MODE_PRIVATE);
    }

    public void setTheme(int theme) {
        SharedPreferences.Editor editor = settingsManager.edit();
        switch (theme) {
            case LIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case NIGHT_MODE:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case SYSTEM_DEFAULT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
        editor.putInt(THEME, theme);
        editor.apply();
    }

    public int getTheme() {
        return settingsManager.getInt(THEME, SYSTEM_DEFAULT);
    }

    public void setCategoryToLoad(int categoryId) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putInt(CATEGORY, categoryId);
        editor.apply();
    }

    public int getCategoryToLoad() {
        return settingsManager.getInt(CATEGORY, ALL_CATEGORIES);
    }

}
