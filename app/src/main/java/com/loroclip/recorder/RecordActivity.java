package com.loroclip.recorder;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import com.loroclip.R;
import com.loroclip.recorder.util.WaveFileHeaderCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class RecordActivity extends Activity {

  private RecorderTask recordTask;

  private PlayerTask playerTask;

  private Button recordStartButton;
  private Button recordPauseButton;
  private Button recordStopButton;
  private Button recordSaveButton;

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

    recordSaveButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//        playerTask = new PlayerTask();
//        playerTask.setWaveData(recordTask.getWaveData());
//        playerTask.start();
        final File savefile = new File(getSavePath(), "test3.wav");

        try {
          byte[] data = recordTask.getWaveData().getByteArray();
          savefile.createNewFile();

          FileOutputStream stream = new FileOutputStream(savefile);
          try {
            WaveFileHeaderCreator.pushWaveHeader(stream, RATE, CHANNEL_CONFIG, AUDIO_ENCODING, data.length);
            stream.write(data);
          } finally {
            if (stream != null) {
              stream.close();
            }
          }
        } catch (IOException ex) {
        }
        String path = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";

        Log.d("Files", "Path: " + path);
        File f = new File(path);
        File file[] = f.listFiles();


        try {
          PlayShortAudioFileViaAudioTrack(path + "test3.wav");
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    });
  }

  private void startRecording() {
    try {
      recordTask = new RecorderTask();
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


  private void PlayShortAudioFileViaAudioTrack(String filePath) throws IOException
  {
    // We keep temporarily filePath globally as we have only two sample sounds now..
    if (filePath==null) {
      return;
    }

    //Reading the file..
    byte[] byteData = null;
    File file = null;
    file = new File(filePath); // for ex. path= "/sdcard/samplesound.pcm" or "/sdcard/samplesound.wav"
    byteData = new byte[(int) file.length()];
    FileInputStream in = null;
    try {
      in = new FileInputStream( file );
      in.read( byteData );
      in.close();
    } catch (FileNotFoundException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
// Set and push to audio track..
    int intSize = android.media.AudioTrack.getMinBufferSize(RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
    AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, RATE, CHANNEL_CONFIG,  AUDIO_ENCODING, intSize, AudioTrack.MODE_STREAM);
    if (at!=null) {
      at.play();
// Write the byte array to the track
      at.write(byteData, 0, byteData.length);
      at.stop();
      at.release();
    }
    else
      Log.d("TCAudio", "audio track is not initialised ");

  }
}