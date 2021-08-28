package com.ilpet.yabm.classes;

public class Category {
    String id;
    String title;

    public Category() { }

    public Category(String id, String title) {
        this.id = id;
        this.title = title;
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
}
