package com.loroclip.model;

import java.util.List;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Record extends SyncableModel<Record> {
    private String title;
    private String note;
    private String file;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<BookmarkHistory> getBookmarkHistories() {
        return BookmarkHistory.find(BookmarkHistory.class, "record = ?", getId().toString());
    }
}
