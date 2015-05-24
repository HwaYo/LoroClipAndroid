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

    public Record() {}

    public static List<Record> findByLocalFilePath(String path) {
        return Record.find(Record.class, "local_file = ?", path);
    }

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
        return this.localFile;
    }

    public void setLocalFilePath(String localFile) {
        this.localFile = localFile;
    }

    public File getLocalFile() {
        if (localFile == null) {
            return new File("");
        }
        return new File(getLocalFilePath());
    }

    public void setLocalFile(File file) {
        this.localFile = file.getAbsolutePath();
    }

    public List<BookmarkHistory> getBookmarkHistories() {
        return BookmarkHistory.find(BookmarkHistory.class, "record = ?", getId().toString());
    }

    public List<FrameGains> getFrameGains() {
        return FrameGains.find(FrameGains.class, "record = ?", getId().toString());
    }

    @Override
    public void overwrite(Record record) {
        super.overwrite(record);
        this.title = record.title;
        this.note = record.note;
        this.file = record.file;
    }

    @Override
    public void delete() {
        List<BookmarkHistory> histories = getBookmarkHistories();
        for (BookmarkHistory history : histories) {
            history.delete();
        }

        File file = getLocalFile();
        if (file.exists()) {
            file.delete();
        }

        List<FrameGains> frameGains = getFrameGains();
        for (FrameGains gains : frameGains) {
            gains.delete();
        }

        super.delete();
    }
}
