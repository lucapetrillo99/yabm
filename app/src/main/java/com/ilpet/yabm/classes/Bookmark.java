package com.ilpet.yabm.classes;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class Bookmark implements Serializable {

    public enum ItemType {
        SIMPLE, NO_DESCRIPTION, NO_IMAGE, NORMAL
    }

    private String id;
    private String link;
    private String title;
    private String description;
    private String image;
    private String category;
    private long reminder;
    private ItemType type;
    private String date;

    public Bookmark(String id, String link, String category, String title, String description,
                    String image, long reminder, ItemType type, String date) {

        this.id = id;
        this.link = link;
        this.category = category;
        this.title = title;
        this.description = description;
        this.image = image;
        this.reminder = reminder;
        this.type = type;
        this.date = date;
    }

    public Bookmark() {}

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public long getReminder() {
        return reminder;
    }

    public void setReminder(long reminder) {
        this.reminder = reminder;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(ItemType type) {
        this.type = type;
    }

    public ItemType getType() {
        return type;
    }

    public void setDate(String date) { this.date = date; }

    public String getDate() { return date; }

    public static Comparator<Bookmark> TitleDescendingOrder = (b1, b2) -> b2.getTitle().compareTo(b1.getTitle());

    public static Comparator<Bookmark> TitleAscendingOrder = Comparator.comparing(Bookmark::getTitle);

    public static Comparator<Bookmark> DateDescendingOrder = (b1, b2) -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1, date2;
        try {
            date1 = dateFormat.parse(b1.getDate());
            date2 = dateFormat.parse(b2.getDate());
            assert date2 != null;
            return date2.compareTo(date1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    };

    public static Comparator<Bookmark> DateAscendingOrder = (b1, b2) -> {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date1, date2;
        try {
            date1 = dateFormat.parse(b1.getDate());
            date2 = dateFormat.parse(b2.getDate());
            assert date1 != null;
            return date1.compareTo(date2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    };
}
