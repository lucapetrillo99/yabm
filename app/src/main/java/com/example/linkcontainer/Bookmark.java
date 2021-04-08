package com.example.linkcontainer;

import java.util.Date;

public class Bookmark {

    String link;
    String title;
    String description;
    String image;
    String category;
    long reminder;

    public Bookmark(String link, String category,String title, String description, String image, 
                    long reminder) {
        this.link = link;
        this.category = category;
        this.title = title;
        this.description = description;
        this.image = image;
        this.reminder = reminder;
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
}
