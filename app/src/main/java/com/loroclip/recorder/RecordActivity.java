
package com.loroclip.recorder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioFormat;
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

import com.loroclip.R;
import com.loroclip.model.Record;

import java.io.File;


public class RecordActivity extends Activity {

  private RecorderTask recordTask;

  private Button recordStartButton;
  private Button recordPauseButton;
  private Button recordStopButton;
  private Button recordSaveButton;
  private AlertDialog saveDialog;
  private WaveDisplayView waveformView;

  private Chronometer chronometer;

  private long stoppedTime;

  private static final int RATE = 44100;
  private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
  private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record);

    recordStartButton = (Button) findViewById(R.id.recordStart);
    recordPauseButton = (Button) findViewById(R.id.recordPause);
    recordStopButton = (Button) findViewById(R.id.recordStop);
    recordSaveButton = (Button) findViewById(R.id.recordSave);

    LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayView);
    waveformView = new WaveDisplayView(getBaseContext());
    displayLayout.addView(waveformView);

    chronometer = (Chronometer) findViewById(R.id.chronometer);

    addEvnetListener();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.menu_record, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    File file = new File(recordTask.getFilePath());
    if(file.exists()) {
      file.delete();
    }
    stopTask(recordTask);
  }

  private void addEvnetListener() {
    recordStartButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        startRecording();
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
      }

    });

    recordPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(recordTask.isPaused()) {
          recordTask.toResume();

          chronometer.setBase(SystemClock.elapsedRealtime() + stoppedTime);
          chronometer.start();
        } else {
          recordTask.pause();
          chronometer.stop();
          stoppedTime = chronometer.getBase() - SystemClock.elapsedRealtime();
        }
      }
    });

    recordStopButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        stopTask(recordTask);
        chronometer.setBase(SystemClock.elapsedRealtime());
        stoppedTime = 0;
        chronometer.stop();
      }
    });

    saveDialog = createSaveDialog();
    recordSaveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        saveDialog.show();
      }
    });
  }

  private void startRecording() {
    try {
      recordTask = new RecorderTask(waveformView);
    } catch (IllegalArgumentException ex) {
    }
    recordTask.start();
  }


  private void stopTask(RecorderTask task) {
    task.stopTask();
    try {
      task.join(1000);
    } catch (InterruptedException e) {
    }
  }

  private File getSavePath() {
    if (hasSDCard()) {
      File path = new File(Environment.getExternalStorageDirectory(), "/Loroclip/");
      path.mkdirs();
      return path;
    } else {
      return getFilesDir();
    }
  }

  private boolean hasSDCard() {
    String state = Environment.getExternalStorageState();
    return state.equals(Environment.MEDIA_MOUNTED);
  }

  private AlertDialog createSaveDialog() {
    final Handler handler = new Handler();
    final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
    return new AlertDialog.Builder(this)
        .setTitle("파일저장")
        .setView(view)
        .setPositiveButton("저장", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            EditText filename = (EditText) view.findViewById(R.id.filenameEditText);
            
            final String newFilePath =  Environment.getExternalStorageDirectory().toString() + "/Loroclip/" + filename.getText() + ".wav";

            File file = new File(recordTask.getFilePath());
            File file2 = new File(newFilePath);

            file.renameTo(file2);
            file.delete();


            handler.post(new Runnable() {
              @Override
              public void run() {
                Toast.makeText(RecordActivity.this, "Save completed: " + newFilePath , Toast.LENGTH_SHORT).show();
              }
            });
            
            Record record = new Record();
            record.setFile(newFilePath);
            record.setTitle(filename.getText() + "");
            record.save();
          }
        })
        .setNegativeButton("취소", null)
        .create();
  }
}