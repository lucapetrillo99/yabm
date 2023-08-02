package com.ilpet.yabm.classes;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Category {
    public enum CategoryProtection {
        UNLOCK(0),
        LOCK(1);

        private final int value;

        CategoryProtection(int value) {
            this.value = value;
        }

        public static CategoryProtection castFromInt(int x) {
            switch (x) {
                case 0:
                    return UNLOCK;
                case 1:
                    return LOCK;
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }

    private String id;
    private String title;
    private String date;
    private CategoryProtection categoryProtection;

    public Category() { }

    public Category(String id, String title, String date, CategoryProtection categoryProtection) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.categoryProtection = categoryProtection;
    }

    public String getCategoryId() {
        return id;
    }

    public String getCategoryTitle() {
        return title;
    }

    public void setCategoryId(String id) {
        this.id = id;
    }

    public void setCategoryTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) { this.date = date; }

    public String getDate() { return date; }

    public void setCategoryProtection(CategoryProtection categoryProtection) {
        this.categoryProtection = categoryProtection;
    }

    public CategoryProtection getPasswordProtection() {
        return categoryProtection;
    }

    public int getPasswordProtectionValue() {
        return categoryProtection.value;
    }

    public static Comparator<Category> TitleDescendingOrder = (c1, c2) -> c2.getCategoryTitle().compareTo(c1.getCategoryTitle());

    public static Comparator<Category> TitleAscendingOrder = Comparator.comparing(Category::getCategoryTitle);

    public static Comparator<Category> DateDescendingOrder = (c1, c2) -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1, date2;
        try {
            date1 = dateFormat.parse(c1.getDate());
            date2 = dateFormat.parse(c2.getDate());
            assert date2 != null;
            return date2.compareTo(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    };

    public static Comparator<Category> DateAscendingOrder = (c1, c2) -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1, date2;
        try {
            date1 = dateFormat.parse(c1.getDate());
            date2 = dateFormat.parse(c2.getDate());
            assert date1 != null;
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    };
}
