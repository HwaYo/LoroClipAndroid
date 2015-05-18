package com.loroclip.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.Date;

/**
 * Created by angdev on 15. 5. 18..
 */
public class ModelSupport<T> extends SugarRecord<T> {
    private boolean deleted;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;

    public boolean isDeleted() {
        return deleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void beforeSave() {}

    @Override
    public void save() {
        if (this.createdAt == null) {
            this.createdAt = new Date();
        }
        this.updatedAt = new Date();

        beforeSave();
        super.save();
    }

    @Override
    public void delete() {
        delete(false);
    }

    public void delete(boolean force) {
        if (force) {
            super.delete();
        } else {
            this.deleted = true;
            save();
        }
    }
}
