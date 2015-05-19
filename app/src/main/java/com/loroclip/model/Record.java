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

    public Record(String title, String file) {
        this.title = title;
        this.file = file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {return title;}
    public String getFile() {return file;}

}
