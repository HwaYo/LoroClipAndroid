package com.loroclip.model;

import com.orm.SugarRecord;

import java.sql.Date;
import java.util.List;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Bookmark extends SugarRecord<Bookmark> {
    private String uuid;
    private String color;
    private String name;
    private Date createdAt;
    private Date updatedAt;


    public Bookmark() {
    }

    public Bookmark(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public List<Bookmark> initializeBookmark(){

        if (Bookmark.listAll(Bookmark.class).size() <= 0){
            new Bookmark("important", String.valueOf(0xffe56673)).save();
            new Bookmark("i dont know", String.valueOf(0xffff66ff)).save();
            new Bookmark("great", String.valueOf(0xFF90FFB0)).save();
            new Bookmark("nothing", String.valueOf(0xFF90FFB0)).save();
        }

        return Bookmark.listAll(Bookmark.class);
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getColor() {
        return Integer.parseInt(color);
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
