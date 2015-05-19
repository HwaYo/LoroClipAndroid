package com.loroclip.record.recorder;

/**
 * Created by stompesi on 15. 5. 16..
 */

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Process;
import android.util.Log;

import com.loroclip.record.View.RecodWaveformView;
import com.loroclip.record.encoder.EncodeFeed;
import com.loroclip.record.encoder.VorbisEncoder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicReference;


public class VorbisRecorder {

  /**
   * 로그 TAG값
   */
  private static final String TAG = "VorbisRecorder";

  /**
   * BUFFER 크기
   */
  private static final int BUFFER_LENGTH = 4096;

  /**
   * 채널 상수
   */
  private static final int CHANNEL_IN_MONO = 1;
  private static final int CHANNEL_IN_STEREO = 2;

  /**
   * quality 상수
   */
  private static final float MINIMUM_QUALITY_VALUE = -0.1f;
  private static final float MAXIMUM_QUALITY_VALUE = 1.0f;

  /**
   * sapleRate : 44100을 가짐
   */
  private long sampleRate;

  /**
   * 채널 수 [1, 2]중 하나를 가짐
   */
  private long numberOfChannels;

  /**
   * quality : [-0.1, 0, 0.1, ..., 1.0]중 하나를 가짐 (원래는 1 ~ 10단계)
   */
  private float quality;

  /**
   * 녹음 상태를 나타냄
   */
    private static enum RecorderState {
    RECORDING, STOPPED, STOPPING, PAUSED
  }

  /**
   * PCM 데이터를 읽거나 Vorbis 데이터를 파일에 쓰는 encodeFeed
   */
  private final EncodeFeed encodeFeed;

  private RecodWaveformView waveForm;

  /**
   * recorder의 현재 상태를 나타냄
   */
  private final AtomicReference<RecorderState> currentState = new AtomicReference<RecorderState>(RecorderState.STOPPED);

  private class FileEncodeFeed implements EncodeFeed {


    /**
     * 쓸 파일
     */
    private final File fileToSaveTo;

    /**
     * Vorbis 데이터를 쓸 output stream
     */
    private OutputStream outputStream;

    /**
     * pcm data를 읽는 audioRecoder
     */
    private AudioRecord audioRecorder;

    /**
     * 인코딩된 vorbis 데이터를 파일에 저장하기 위한 클레스의 생성자
     *
     * @param fileToSaveTo 쓸파일
     */
    public FileEncodeFeed(File fileToSaveTo) {
      if (fileToSaveTo == null) {
        throw new IllegalArgumentException("File to save to must not be null");
      }
      this.fileToSaveTo = fileToSaveTo;

    }

    @Override
    public long readPCMData(byte[] pcmDataBuffer, int amountToRead) {
      if (isStopped() || isStopping()) {
        return 0;
      }

      int read = 0;
      do {
        read = audioRecorder.read(pcmDataBuffer, 0, amountToRead);
      } while(isPaused());

      switch (read) {
        case AudioRecord.ERROR_INVALID_OPERATION:
          Log.e(TAG, "Invalid operation on AudioRecord object");
          return 0;
        case AudioRecord.ERROR_BAD_VALUE:
          Log.e(TAG, "Invalid value returned from audio recorder");
          return 0;
        case -1:
          return 0;
        default:
          // 성공적으로 pcm 읽음
          waveForm.addWaveData(pcmDataBuffer);
          return read;
      }
    }

    @Override
    public int writeVorbisData(byte[] vorbisData, int amountToWrite) {
      if (vorbisData != null && amountToWrite > 0 && outputStream != null && !isStopped()) {
        try {
          outputStream.write(vorbisData, 0, amountToWrite);
          return amountToWrite;
        } catch (IOException e) {
          // 파일쓰기 실패
          Log.e(TAG, "Failed to write data to file, stopping recording", e);
          stop();
        }
      }
      return 0;
    }

    @Override
    public void stop() {

      if (isRecording() || isStopping() || isPaused()) {
        // 멈춤상태 등록
        currentState.set(RecorderState.STOPPED);
        waveForm.clearWaveData();

        // outputStream 닫기
        if (outputStream != null) {
          try {
            outputStream.flush();
            outputStream.close();
          } catch (IOException e) {
            Log.e(TAG, "Failed to close output stream", e);
          }
          outputStream = null;
        }

        // audioRecorder 중지
        if (audioRecorder != null) {
          audioRecorder.stop();
          audioRecorder.release();
        }
      }
    }

    @Override
    public void stopEncoding() {
      if (isRecording() || isPaused()) {
        currentState.set(RecorderState.STOPPING);
      }
    }

    @Override
    public void start() {
      if (isStopped()) {

        // audioRecorde 생성
        int channelConfiguration = numberOfChannels == CHANNEL_IN_MONO ? AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO;
        int bufferSize = AudioRecord.getMinBufferSize((int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT);

        audioRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, (int) sampleRate, channelConfiguration, AudioFormat.ENCODING_PCM_16BIT, bufferSize);

        // 녹음 시작
        currentState.set(RecorderState.RECORDING);
        audioRecorder.startRecording();

        // output stream 생성
        if (outputStream == null) {
          try {
            outputStream = new BufferedOutputStream(new FileOutputStream(fileToSaveTo));
          } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to write to file", e);
          }
        }
      }
    }
  }

  /**
   * VorbisRecorder 생성자
   *
   * @param fileToSaveTo 저장할 파일
   * @param waveForm waveform을 보여주는 view
   */
  public VorbisRecorder(File fileToSaveTo, RecodWaveformView waveForm) {
    if (fileToSaveTo == null) {
      throw new IllegalArgumentException("File to play must not be null.");
    }

    // 파일 존재시 삭제
    if (fileToSaveTo.exists()) {
      fileToSaveTo.deleteOnExit();
    }

    this.waveForm = waveForm;
    this.waveForm.setDrawData(BUFFER_LENGTH);
    this.encodeFeed = new FileEncodeFeed(fileToSaveTo);
  }

  /**
   * 녹음 / 인코딩 시작
   *
   * @param sampleRate
   * @param numberOfChannels
   * @param quality
   */
  @SuppressWarnings("all")
  public synchronized void start(long sampleRate, long numberOfChannels, float quality) {
    if (isStopped()) {
      if (numberOfChannels != CHANNEL_IN_MONO && numberOfChannels != CHANNEL_IN_STEREO) {
        throw new IllegalArgumentException("Channels can only be one or two");
      }
      if (sampleRate <= 0) {
        throw new IllegalArgumentException("Invalid sample rate, must be above 0");
      }
      if (quality < MINIMUM_QUALITY_VALUE || quality > MAXIMUM_QUALITY_VALUE) {
        throw new IllegalArgumentException("Quality must be between -0.1 and 1.0");
      }

      this.sampleRate = sampleRate;
      this.numberOfChannels = numberOfChannels;
      this.quality = quality;

      // 녹음 시작
      new Thread(new AsyncEncoding()).start();
    }
  }

  /**
   * 녹음 / 인코딩 중지
   */
  public synchronized void stop() {
    encodeFeed.stopEncoding();
  }

  public synchronized void pause() {
    currentState.set(RecorderState.PAUSED);
  }

  public synchronized void restart() {
    currentState.set(RecorderState.RECORDING);
  }

  /**
   * background thread 작업처리
   */
  private class AsyncEncoding implements Runnable {
    @Override
    public void run() {
      //
      Log.d(TAG, "Start the native encoder");
      Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

      int result = VorbisEncoder.startEncodingWithQuality(sampleRate, numberOfChannels, quality, encodeFeed);

      switch (result) {
        case EncodeFeed.SUCCESS:
          Log.d(TAG, "Encoder successfully finished");
          break;
        case EncodeFeed.ERROR_INITIALIZING:
          Log.e(TAG, "There was an error initializing the native encoder");
          break;
        default:
          Log.e(TAG, "Encoder returned an unknown result code");
          break;
      }
    }
  }




  /**
   * 녹음중인지 확인
   *
   * @return 녹음중이면[RECORDING] true, 아니면[STOPPED, STOPPING, PAUSED] false
   */
  public synchronized boolean isRecording() {
    return currentState.get() == RecorderState.RECORDING;
  }

  /**
   * 녹음이 중지되었는지 확인
   *
   * @return 중지 되었으면[STOPPED] true, 아니면[STOPPING, RECORDING, PAUSED] false
   */
  public synchronized boolean isStopped() {
    return currentState.get() == RecorderState.STOPPED;
  }

  /**
   * 녹음 중지중인지 확인
   *
   * @return 중지 중이면[STOPPING] true, 아니면[STOPPED, RECORDING, PAUSED] false
   */
  public synchronized boolean isStopping() {
    return currentState.get() == RecorderState.STOPPING;
  }

  /**
   * 녹음 정지중인지 확인
   *
   * @return 중지 중이면[PAUSED] true, 아니면[STOPPED, RECORDING, STOPPING] false
   */
  public synchronized boolean isPaused() {
    return currentState.get() == RecorderState.PAUSED;
  }
}