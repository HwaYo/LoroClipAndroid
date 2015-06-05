package com.loroclip.model;

import java.util.List;

/**
 * Created by angdev on 15. 5. 11..
 */
public class Bookmark extends SyncableModel<Bookmark> {
    private String color;
    private String name;

    public Bookmark() {}

    public Bookmark(String name, String color) {
        this.name = name;
        this.color = color;
    }

    public int getColor() {
        if (color == null) {
            return 0;
        }
        return (int)Long.parseLong(color.replace("#", "ff"), 16);
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<BookmarkHistory> getBookmarkHistories() {
        return BookmarkHistory.find(BookmarkHistory.class, "bookmark = ? AND deleted = 0 ORDER BY start ASC", getId().toString());
    }

    @Override
    public void overwrite(Bookmark bookmark) {
        super.overwrite(bookmark);
        this.name = bookmark.name;
        this.color = bookmark.color;
    }

    @Override
    public void delete(boolean force) {
        List<BookmarkHistory> histories = getBookmarkHistories();
        for (BookmarkHistory history : histories) {
            history.delete(force);
        }

        super.delete(force);
    }
}
