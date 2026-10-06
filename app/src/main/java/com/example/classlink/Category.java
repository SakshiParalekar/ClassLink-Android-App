package com.example.classlink; // Make sure this matches your package name

public class Category {
    private String name;
    private int iconResId; // Drawable resource ID for the icon

    public Category(String name, int iconResId) {
        this.name = name;
        this.iconResId = iconResId;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }
}