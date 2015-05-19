package com.loroclip.model;

import com.orm.SugarRecord;

/**
 * Created by minhyeok on 5/18/15.
 */
public class FrameGains extends SugarRecord<FrameGains> {
  String frames;

  Record record;

  public String getFrames() {
    return frames;
  }

  public FrameGains() {
  }

  public FrameGains(String frames) {
    this.frames = frames;
  }

  public Record getRecord() {
    return record;
  }

  public void setRecord(Record record) {
    this.record = record;
  }
}