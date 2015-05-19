package com.loroclip.model;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Bookmark extends SyncableModel<Bookmark> {
    private String color;
    private String name;

    public Bookmark() {}

    public Bookmark(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public int getColor() {
        if (color == null) {
            return 0;
        }
        return (int)Long.parseLong(color.replace("#", "ff"), 16);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
