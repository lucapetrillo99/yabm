package com.example.linkcontainer;

public class Category {
    String id;
    String title;
    String image;

    public Category() {

    }

    public Category(String id, String title, String image) {

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

    public String getCategoryImage() {
        return image;
    }

    public void setCategoryId(String id) {
        this.id = id;
    }

    public void setCategoryTitle(String title) {
        this.title = title;
    }

    public void setCategoryImage(String image) {
        this.image = image;
    }
}
