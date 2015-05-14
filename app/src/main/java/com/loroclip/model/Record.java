package com.loroclip.model;

import com.orm.SugarRecord;

import java.sql.Date;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Record extends SugarRecord<Record> {
    public String uuid;
    public String title;
    public String note;
    public String bookmark;
    public String file;
    public boolean deleted;
    public Date createdAt;
    public Date updatedAt;

    public Record(){
    }
}
