package com.loroclip.model;

/**
 * Created by minhyeok on 5/18/15.
 */
public class FrameGains extends ModelSupport<FrameGains> {
  private String frames;
  private Record record;

  public FrameGains() {}

  public String getFrames() {
    return frames;
  }

  public void setFrames(String frames) {
    this.frames = frames;
  }

  public Record getRecord() {
    return record;
  }

  public void setRecord(Record record) {
    this.record = record;
  }
}

