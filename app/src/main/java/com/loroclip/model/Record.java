package com.loroclip.model;

import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.util.Date;
import java.util.List;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Record extends SugarRecord<Record> {
    public String uuid;
    public String title;
    public String note;
    public String file;
    public boolean deleted;
    @SerializedName("created_at")
    public Date createdAt;
    @SerializedName("updated_at")
    public Date updatedAt;
    @SerializedName("synced_at")
    public Date syncedAt;
    public boolean dirty;

    public Record(){
    }

    public static Record findByUUID(String uuid) {
        List<Record> records = Record.findWithQuery(Record.class, "SELECT * FROM record WHERE uuid = ?", uuid);
        if (records.isEmpty()) {
            return null;
        }
        return records.get(0);
    }

    public static Date getOldestSyncedAt() {
        List<Record> records = Record.findWithQuery(Record.class, "SELECT * FROM record ORDER BY synced_at ASC");
        if (records.isEmpty()) {
            return new Date(0);
        }

        Date syncedAt = records.get(0).syncedAt;
        return syncedAt != null? syncedAt : new Date(0);
    }

    // TODO: Review
    public void overwriteEntity(Record record) {
        record.setId(this.getId());
        record.save(true);
    }

    @Override
    public void save() { save(false); }

    public void save(boolean synced) {
        if (synced) {
            this.dirty = false;
            this.syncedAt = new Date();
        }
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
        }
    }
}
