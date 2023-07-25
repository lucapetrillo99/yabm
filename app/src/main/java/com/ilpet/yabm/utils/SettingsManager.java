package com.ilpet.yabm.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

public class SettingsManager {
    private final SharedPreferences settingsManager;
    private static final String THEME = "theme";
    private static final String CATEGORY = "category";
    private static final String AUTO_BACKUP = "auto_backup";
    private static final String AUTO_BACKUP_URI = "auto_backup_uri";
    private static final String FIRST_ACCESS = "first_access";
    private static final String ALL_BOOKMARKS = "Tutti i segnalibri";
    private static final String BOOKMARK_ORDER_BY = "bookmark_order_by";
    private static final String BOOKMARK_ORDER_TYPE = "bookmark_order_type";
    private static final String CATEGORY_ORDER_BY = "category_order_by";
    private static final String CATEGORY_ORDER_TYPE = "category_order_type";
    private static final String EXPORTING_BOOKMARKS = "exporting_bookmarks";
    private static final int SYSTEM_DEFAULT = 0;
    private static final int LIGHT_MODE = 1;
    private static final int NIGHT_MODE = 2;
    public enum SortOrder {
        date, title, ASC, DESC
    }

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

    public void setCategory(String category) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(CATEGORY, category);
        editor.apply();
    }

    public String getCategory() {
        return settingsManager.getString(CATEGORY, ALL_BOOKMARKS);
    }

    public void setAutoBackup(boolean autoBackup) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putBoolean(AUTO_BACKUP, autoBackup);
        editor.apply();
    }

    public boolean getAutoBackup() {
        return settingsManager.getBoolean(AUTO_BACKUP, false);
    }

    public void setFirstAccess(boolean firstAccess) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putBoolean(FIRST_ACCESS, firstAccess);
        editor.apply();
    }

    public boolean isFirstAccess() {
        return settingsManager.getBoolean(FIRST_ACCESS, true);
    }

    public void setAutoBackupUri(String uri) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(AUTO_BACKUP_URI, uri);
        editor.apply();
    }

    public String getAutoBackupUri() {
        return settingsManager.getString(AUTO_BACKUP_URI, null);
    }

    public void setBookmarkOrderBy(SortOrder sortOrder) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(BOOKMARK_ORDER_BY, String.valueOf(sortOrder));
        editor.apply();
    }

    public String getBookmarkOrderBy() {
        return settingsManager.getString(BOOKMARK_ORDER_BY, String.valueOf(SortOrder.date));
    }

    public void setBookmarkOrderType(SortOrder sortOrder) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(BOOKMARK_ORDER_TYPE, String.valueOf(sortOrder));
        editor.apply();
    }

    public String getBookmarkOrderType() {
        return settingsManager.getString(BOOKMARK_ORDER_TYPE, String.valueOf(SortOrder.ASC));
    }

    public void setCategoryOrderBy(SortOrder sortOrder) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(CATEGORY_ORDER_BY, String.valueOf(sortOrder));
        editor.apply();
    }

    public String getCategoryOrderBy() {
        return settingsManager.getString(CATEGORY_ORDER_BY, String.valueOf(SortOrder.date));
    }

    public void setCategoryOrderType(SortOrder sortOrder) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putString(CATEGORY_ORDER_TYPE, String.valueOf(sortOrder));
        editor.apply();
    }

    public String getCategoryOrderType() {
        return settingsManager.getString(CATEGORY_ORDER_TYPE, String.valueOf(SortOrder.ASC));
    }

    public void setBookmarksExporting(boolean option) {
        SharedPreferences.Editor editor = settingsManager.edit();
        editor.putBoolean(EXPORTING_BOOKMARKS, option);
        editor.apply();
    }

    public boolean getBookmarksExporting() {
        return settingsManager.getBoolean(EXPORTING_BOOKMARKS, false);
    }
}
