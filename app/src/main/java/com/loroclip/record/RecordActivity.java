
package com.loroclip.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loroclip.LoroClipSelectActivity;
import com.loroclip.R;
import com.loroclip.model.Record;
import com.loroclip.record.View.RecodWaveformView;
import com.loroclip.record.recorder.VorbisRecorder;

import java.io.File;


public class RecordActivity extends Activity {

  private Button recordStartButton;
  private Button recordPauseButton;
  private Button recordStopButton;
  private Button recordRestartButton;
  private RecodWaveformView waveformView;
  private AlertDialog saveDialog;

  private RecorderHandler recorderHandler;
  private TimerHandler timerHandler;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);

    recordStartButton = (Button) findViewById(R.id.recordStart);
    recordPauseButton = (Button) findViewById(R.id.recordPause);
    recordStopButton = (Button) findViewById(R.id.recordStop);
    recordRestartButton = (Button) findViewById(R.id.recordRestart);


    LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayView);
    waveformView = new RecodWaveformView(getBaseContext());
    displayLayout.addView(waveformView);

    this.recorderHandler = new RecorderHandler();
    this.timerHandler = new TimerHandler();
    addEventListener();


  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    recorderHandler.stop();
    recorderHandler.deleteTempAudioRecordFile();
  }

  private void addEventListener() {
    recordStartButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.start();
        timerHandler.start();

        recordStartButton.setVisibility(View.GONE);
        recordPauseButton.setVisibility(View.VISIBLE);
        recordStopButton.setVisibility(View.VISIBLE);
      }
    });

    recordPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.pause();
        timerHandler.pause();

        recordPauseButton.setVisibility(View.GONE);
        recordRestartButton.setVisibility(View.VISIBLE);
      }
    });

    recordRestartButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.restart();
        timerHandler.restart();

        recordRestartButton.setVisibility(View.GONE);
        recordPauseButton.setVisibility(View.VISIBLE);
      }
    });

    saveDialog = createSaveDialog();
    recordStopButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.stop();
        timerHandler.stop();
        saveDialog.show();

        recordStopButton.setVisibility(View.GONE);
        recordPauseButton.setVisibility(View.GONE);
        recordRestartButton.setVisibility(View.GONE);
        recordStartButton.setVisibility(View.VISIBLE);
      }
    });
  }

  private class RecorderHandler {
    private VorbisRecorder loroclipRecorder;

    private final int LOROCLIP_AUDIO_CHANNELS = 2;
    private final float LOROCLIP_AUDIO_QUALITY = 0.7f;
    private final int LOROCLIP_AUDIO_SAMPLE_RATE = 44100;

    private final String AUDIO_OGG_EXTENSION = ".ogg";
    private final String LOROCLIP_TEMP_RECORDING_FILE_NAME = "loroclip_temp_recording_file";
    private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";;

    public void start() {
      File loroclipPath = new File(LOROCLIP_PATH);
      if(!loroclipPath.exists()) {
        loroclipPath.mkdirs();
      }

      if (loroclipRecorder == null || loroclipRecorder.isStopped()) {
        File fileToSaveTo = new File(LOROCLIP_PATH, LOROCLIP_TEMP_RECORDING_FILE_NAME + AUDIO_OGG_EXTENSION);

        //Create our recorder if necessary
        if (loroclipRecorder == null) {
          loroclipRecorder = new VorbisRecorder(fileToSaveTo, waveformView);
        }
        loroclipRecorder.start(LOROCLIP_AUDIO_SAMPLE_RATE, LOROCLIP_AUDIO_CHANNELS, LOROCLIP_AUDIO_QUALITY);
      }
    }

    private void pause() {
      loroclipRecorder.pause();
    }

    private void restart() {
      loroclipRecorder.restart();
    }

    private void stop() {
      if (loroclipRecorder != null && (loroclipRecorder.isRecording() || loroclipRecorder.isPaused())) {
        loroclipRecorder.stop();
      }
    }

    public void recordFileSave(String fileName) {
      final Handler handler = new Handler();
      String fromFilePath = LOROCLIP_PATH + LOROCLIP_TEMP_RECORDING_FILE_NAME + AUDIO_OGG_EXTENSION;
      String newFilePath =  LOROCLIP_PATH + fileName + AUDIO_OGG_EXTENSION;

      File from = new File(fromFilePath);
      File to = new File(newFilePath);

      while (to.exists()){
        fileName = fileName.concat("_dup");
        newFilePath =  LOROCLIP_PATH + fileName + AUDIO_OGG_EXTENSION;
        to = new File(newFilePath);
      }

      from.renameTo(to);

      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(RecordActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
      });

      Record record = new Record();
      record.setFile(newFilePath);
      record.setTitle(fileName);
      record.save();
      finish();
    }

      public void deleteTempAudioRecordFile() {
      File tempAudioRecordFile = new File(LOROCLIP_PATH, LOROCLIP_TEMP_RECORDING_FILE_NAME + AUDIO_OGG_EXTENSION);
      if(tempAudioRecordFile.exists()){
        tempAudioRecordFile.deleteOnExit();
      }
    }
  }


  private class TimerHandler {
    private Chronometer chronometer;
    private long stoppedTime;

    public TimerHandler() {
      chronometer = (Chronometer) findViewById(R.id.chronometer);
    }
    public void start() {
      chronometer.setBase(SystemClock.elapsedRealtime());
      chronometer.start();
    }

    public void pause() {
      chronometer.stop();
      stoppedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
    }

    public void restart() {
      chronometer.setBase(SystemClock.elapsedRealtime() + stoppedTime);
      chronometer.start();
    }

    public void stop() {
      chronometer.setBase(SystemClock.elapsedRealtime());
      stoppedTime = 0;
      chronometer.stop();
    }
  }

  private AlertDialog createSaveDialog() {
    final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
    return new AlertDialog.Builder(this)
        .setTitle("파일저장 (저장안함을 누를시 현재 녹음파일을 되돌릴수 없습니다.!!)")
        .setView(view)
        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            EditText filename = (EditText) view.findViewById(R.id.filenameEditText);
            recorderHandler.recordFileSave(filename.getText().toString());
          }
        })
        .setNegativeButton("저장안함", null)
        .create();
  }
}