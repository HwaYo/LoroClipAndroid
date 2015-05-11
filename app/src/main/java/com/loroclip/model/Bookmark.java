package com.loroclip.model;

import com.orm.SugarRecord;

import java.sql.Date;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Bookmark extends SugarRecord<Bookmark> {
    public String uuid;
    public String color;
    public String name;
    public Date createdAt;
    public Date updatedAt;
}
