package com.loroclip.recorder;

/**
 * Created by stompesi on 15. 5. 9..
 */

public class LoroclipRecoder {

  private static final int STATE_ERROR = -1;
  private static final int STATE_READY = 0;
  private static final int STATE_RECORDING = 1;
  private static final int STATE_PAUSED = 2;

  private byte[] buffer;
  private int bufferSize;


  public LoroclipRecoder() {
  }

}
