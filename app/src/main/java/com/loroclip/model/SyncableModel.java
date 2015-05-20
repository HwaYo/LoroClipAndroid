package com.loroclip.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by angdev on 15. 5. 18..
 */
public abstract class SyncableModel<T extends SyncableModel> extends ModelSupport<T> {
    private String uuid;
    @SerializedName("synced_at")
    private Date syncedAt;
    private boolean dirty;

    public String getUuid() {
        return uuid;
    }

    public Date getSyncedAt() {
        return syncedAt;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public static <T extends SyncableModel> T findByUuid(Class<T> type, String uuid) {
        List<T> entities = T.find(type, "uuid = ?", uuid);
        if (entities.isEmpty()) {
            return null;
        }
        return entities.get(0);
    }

    public static <T extends SyncableModel> Date getRecentSyncedAt(Class<T> type) {
        String query = String.format("SELECT * FROM %s ORDER BY synced_at DESC", getTableName(type));
        List<T> entities = T.findWithQuery(type, query);
        if (entities.isEmpty()) {
            return new Date(0);
        }

        Date syncedAt = entities.get(0).getSyncedAt();
        return syncedAt != null? syncedAt : new Date(0);
    }

    public void saveAsSynced() {
        setDirty(false);
        this.syncedAt = new Date();
        super.save();
    }

    @Override
    public void overwrite(T t) {
        super.overwrite(t);

        SyncableModel model = t;
        this.uuid = model.uuid;
        this.syncedAt = model.syncedAt;
        this.dirty = model.dirty;
    }

    @Override
    public void save() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID().toString();
        }
        if (this.syncedAt == null) {
            this.syncedAt = new Date(0);
        }
        setDirty(true);
        super.save();
    }
}
