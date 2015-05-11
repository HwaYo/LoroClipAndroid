package com.loroclip.recorder;

import java.io.ByteArrayOutputStream;

/**
 * Created by stompesi on 15. 5. 9..
 */
public class WaveData {
  private ByteArrayOutputStream waveData;

  public WaveData() {
    waveData = new ByteArrayOutputStream();
  }

  public void addWaveData(byte[] data) {
    addWaveData(data, 0, data.length);
  }
  public void addWaveData(byte[] data, int offset, int len) {
    waveData.write(data, offset, len);
  }

  public byte[] getByteArray() {
    return waveData.toByteArray();
  }
}
