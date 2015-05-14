package com.loroclip.model;

import com.google.gson.annotations.SerializedName;
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
    @SerializedName("created_at")
    public Date createdAt;
    @SerializedName("updated_at")
    public Date updatedAt;

    public Record(){
    }
}
