package com.loroclip.recorder;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by stompesi on 15. 5. 9..
 */
public class PlayerTask extends Thread{

  private AudioTrack player;
  private InputStream rawInput;

  private byte[] buffer;
  private final int bufferSize;
  private static final int RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
  private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  private boolean isRunning;

  public PlayerTask(){
    this.bufferSize = AudioTrack.getMinBufferSize(RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
    this.player = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize, AudioTrack.MODE_STREAM);
    this.buffer = new byte[bufferSize];
    this.isRunning = false;
  }

  public void setWaveData(byte[] data) {
    this.rawInput = new ByteArrayInputStream(data);
  }


  @Override
  public void run() {
    isRunning = true;

    player.play();
    try {
      byte[] buffer = new byte[bufferSize];

      int len = 0;
      while (isRunning && (len = rawInput.read(buffer)) != -1) {
        player.write(buffer, 0, len);
      }
    } catch (IOException ex) {
    } finally {
      player.stop();
      player.release();
      isRunning = false;
    }
  }
}


