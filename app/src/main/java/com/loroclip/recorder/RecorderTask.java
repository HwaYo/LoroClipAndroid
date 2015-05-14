package com.loroclip.recorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderTask extends Thread {

	private final AudioRecord recorder;
	private byte[] buffer;
	private final int bufferSize;
	private boolean isRunning;
	private boolean isPaused;


	private WaveDisplayView waveForm;

	private static final int RATE = 44100;
	private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
	private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;


	FileOutputStream out;
	String path, fileName;

	public RecorderTask(WaveDisplayView waveForm) throws IllegalArgumentException {
		this.bufferSize = AudioRecord.getMinBufferSize(RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
		this.buffer = new byte[bufferSize];
		this.recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, bufferSize);
		this.waveForm = waveForm;
		this.isRunning = false;
		this.isPaused = false;
		this.waveForm.setDrawData(bufferSize);

		makeFile();
	}

	public void makeFile() {
		path = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";
		fileName = new SimpleDateFormat("yyyy-MM-dd-ss").format(new Date()) + ".wav";
//		Log.d("files", String.valueOf(data.length));
		try {
			out = new FileOutputStream( new File(path, fileName));
			WaveFileHeaderCreator.pushWaveHeader(out, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, 0);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String getFilePath() {
		return path + fileName;
	}

	@Override
	public void run() {
		isRunning = true;
		recorder.startRecording();


		//
		try {
			while (isRunning) {
    if(!isPaused) {
     int len = recorder.read(buffer, 0, buffer.length);
			out.write(buffer, 0, len);
			waveForm.addWaveData(buffer, 0, len);
		}
			}
		} catch (IOException e) {
			e.printStackTrace();
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