package com.loroclip.model;

import java.io.File;
import java.util.List;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Record extends SyncableModel<Record> {
    private String title;
    private String note;
    private String file;
    private String localFile;

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

    public void setRemoteFilePath(String remoteFile) {
        this.file = remoteFile;
    }

    public String getRemoteFilePath() {
        return file;
    }

    public String getLocalFilePath() {
        return localFile;
    }

    public void setLocalFilePath(String localFile) {
        this.localFile = localFile;
    }

    public File getLocalFile() {
        return new File(localFile);

    }

    public List<BookmarkHistory> getBookmarkHistories() {
        return BookmarkHistory.find(BookmarkHistory.class, "record = ?", getId().toString());
    }
}
