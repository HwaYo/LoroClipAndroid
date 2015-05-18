package com.loroclip.model;

import com.orm.dsl.Ignore;

/**
 * Created by minhyeok on 5/12/15.
 */
public class BookmarkHistory extends SyncableModel<BookmarkHistory>{
    private float start;
    private float end;

    @Ignore
    public String record_uuid;
    private Record record;

    @Ignore
    public String bookmark_uuid;
    private Bookmark bookmark;

    public BookmarkHistory() {
    }

    public BookmarkHistory(float start) {
        this.start = start;
    }

    public String getName() {
        if (bookmark == null) {
            return "undefined";
        }
        return bookmark.getName();
    }

    public int getColor() {
        if (bookmark == null) {
            return 0;
        }
        return bookmark.getColor();
    }

    public float getStart() {
        return start;
    }

    public int getStartMiiliseconds() {
        return (int) (start * 1000);
    }

    public void setStart(float start) {
        this.start = start;
    }

    public float getEnd() {
        return end;
    }

    public int getEndMiliseconds() {
        return (int) (end * 1000);
    }

    public void setEnd(float end) {
        this.end = end;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public void setBookmark(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public void beforeSave() {
        if (record_uuid != null) {
            record = Record.findByUuid(Record.class, record_uuid);
        }

        if (bookmark_uuid != null) {
            bookmark = Bookmark.findByUuid(Bookmark.class, bookmark_uuid);
        }
    }
}
