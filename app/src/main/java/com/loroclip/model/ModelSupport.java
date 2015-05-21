package com.loroclip.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by angdev on 15. 5. 18..
 */
public abstract class ModelSupport<T extends ModelSupport> extends SugarRecord<T> {
    private boolean deleted;
    @SerializedName("created_at")
    private Date createdAt;
    @SerializedName("updated_at")
    private Date updatedAt;

    public static <T extends SugarRecord> List<T> listExists(Class<T> type) {
        return T.find(type, "deleted = ?", "0");
    }

    public boolean isDeleted() {
        return deleted;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void overwrite(T t) {
        ModelSupport model = t;
        this.createdAt = model.createdAt;
        this.updatedAt = model.updatedAt;
        this.deleted = model.deleted;
    }

    public void beforeSave() {}

    public void decorate() {}

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
