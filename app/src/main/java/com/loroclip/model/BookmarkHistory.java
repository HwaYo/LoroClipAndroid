package com.loroclip.model;

import com.orm.SugarRecord;

/**
 * Created by minhyeok on 5/12/15.
 */
public class BookmarkHistory extends SugarRecord<BookmarkHistory>{
    private int start;
    private int end;
    private String name;
    private int color;
    private String filename;

    private Record record;

    public BookmarkHistory() {
    }

    public BookmarkHistory(int start, String filename, int color, String name) {
        this.filename = filename;
        this.color = color;
        this.name = name;
        this.start = start;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
