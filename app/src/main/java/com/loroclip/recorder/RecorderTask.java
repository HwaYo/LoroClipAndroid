package com.loroclip.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

public class RecorderTask extends Thread {

	private final AudioRecord recorder;
	private byte[] buffer;
	private final int bufferSize;
	private boolean isRunning;
	private boolean isPaused;


	private WaveData waveData;

	private static final int RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

	public RecorderTask() throws IllegalArgumentException {
		this.bufferSize = AudioRecord.getMinBufferSize(RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
		this.buffer = new byte[bufferSize];
		this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize);
		this.waveData = new WaveData();
		this.isRunning = false;
		this.isPaused = false;
	}

	@Override
	public void run() {
		isRunning = true;
		recorder.startRecording();
		try {
			while (isRunning) {
				if(!isPaused) {
					int len = recorder.read(buffer, 0, buffer.length);
					waveData.addWaveData(buffer, 0, len);
				}
			}
		} finally {
			isRunning = false;
			isPaused = false;
			recorder.stop();
			recorder.release();
		}
	}

	public void stopTask() {
		this.isRunning = false;
	}

	public WaveData getWaveData() {
		return waveData;
	}

	public boolean isPaused() {
		return isPaused;
	}

	public void pause() {
		isPaused = true;
	}

	public void toResume() {
		isPaused = false;
	}
}
