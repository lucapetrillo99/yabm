package com.example.linkcontainer.classes;

import android.graphics.Bitmap;

public class Category {
    String id;
    String title;
    Bitmap image;

    public Category() { }

    public Category(String id, String title, Bitmap image) {
        this.id = id;
        this.title = title;
        this.image = image;
    }

    public String getCategoryId() {
        return id;
    }

    public String getCategoryTitle() {
        return title;
    }

    public Bitmap getCategoryImage() {
        return image;
    }

    public void setCategoryId(String id) {
        this.id = id;
    }

    public void setCategoryTitle(String title) {
        this.title = title;
    }

    public void setCategoryImage(Bitmap image) {
        this.image = image;
    }
}
