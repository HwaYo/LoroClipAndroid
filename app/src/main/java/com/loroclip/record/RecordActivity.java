
package com.loroclip.record;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loroclip.BookmarkListAdapter;
import com.loroclip.R;
import com.loroclip.model.FrameGains;
import com.loroclip.model.Record;
import com.loroclip.record.View.RecodWaveformView;
import com.loroclip.record.recorder.VorbisRecorder;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class RecordActivity extends ActionBarActivity {

  private ImageView recordStartButton;
  private Button recordPauseButton;
  private ImageView recordStopButton;
  private Button recordRestartButton;
  private RecodWaveformView waveformView;
  private AlertDialog saveDialog;

  private RecorderHandler recorderHandler;
  private TimerHandler timerHandler;

  private Toolbar mToolbar;

  private BookmarkListAdapter bookmarkListAdapter;
  private RecyclerView bookmarkRecycler;
  private LinearLayoutManager manager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_new);

    mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
    setSupportActionBar(mToolbar);

    recordStartButton = (ImageView) findViewById(R.id.record_action_img);
    recordPauseButton = (Button) findViewById(R.id.tmpBtn);
    recordStopButton = (ImageView) findViewById(R.id.record_done_img);
    recordRestartButton = (Button) findViewById(R.id.tmpBtn);

    bookmarkRecycler = (RecyclerView) findViewById(R.id.bookmark_list);
    bookmarkListAdapter = new BookmarkListAdapter(this, bookmarkRecycler);
    manager = new LinearLayoutManager(this);
    manager.setOrientation(OrientationHelper.VERTICAL);

    bookmarkRecycler.setLayoutManager(manager);
    bookmarkRecycler.setAdapter(bookmarkListAdapter);
    bookmarkRecycler.addItemDecoration(
            new HorizontalDividerItemDecoration
                    .Builder(this)
                    .sizeResId(R.dimen.divider)
                    .color(R.color.myGrayColor)
                    .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                    .build());

    LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayViewTmp);
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

//        recordStartButton.setVisibility(View.GONE);
//        recordPauseButton.setVisibility(View.VISIBLE);
//        recordStopButton.setVisibility(View.VISIBLE);
      }
    });

    recordPauseButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.pause();
        timerHandler.pause();

//        recordPauseButton.setVisibility(View.GONE);
//        recordRestartButton.setVisibility(View.VISIBLE);
      }
    });

    recordRestartButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.restart();
        timerHandler.restart();

//        recordRestartButton.setVisibility(View.GONE);
//        recordPauseButton.setVisibility(View.VISIBLE);
      }
    });

//    saveDialog = createSaveDialog();

    recordStopButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        recorderHandler.stop();
        timerHandler.stop();

//        saveDialog.show();
        showSaveDialog();

//        recordStopButton.setVisibility(View.GONE);
//        recordPauseButton.setVisibility(View.GONE);
//        recordRestartButton.setVisibility(View.GONE);
//        recordStartButton.setVisibility(View.VISIBLE);
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
      waveformView.startRecord();
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

      Record record = new Record();
      record.setLocalFile(to);
      record.setTitle(fileName);
      record.save();

      FrameGains fg = new FrameGains();
      fg.setFrames(waveformView.getJsonArray().toString());
      fg.setRecord(record);
      fg.save();

      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(RecordActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
      });

      Intent intent = new Intent();
      intent.putExtra("record_id", record.getId());
      setResult(Activity.RESULT_OK, intent);
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
    EditText saveFile = (EditText)view.findViewById(R.id.filenameEditText);
    saveFile.addTextChangedListener(new TextWatcher() {

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
          saveDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        } else {
          saveDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
        }
      }

      @Override // 입력이 끝났을 때
      public void afterTextChanged(Editable s) {
      }

      @Override // 입력하기 전에
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });

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
        .setNegativeButton("저장안함", new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            EditText filename = (EditText) view.findViewById(R.id.filenameEditText);
            filename.setText("");
            saveDialog.dismiss();
          }
        })
        .create();
  }

  private void showSaveDialog() {
    DateFormat format = new SimpleDateFormat("HH:mm, yyyy-MM-dd");
    String dateString = "Record at " + format.format(Calendar.getInstance().getTime());

    // show a dialog to set filename
    final MaterialDialog dialog = new MaterialDialog.Builder(this)
            .title(R.string.edit_name)
            .content(R.string.set_record_name)
            .inputType(InputType.TYPE_CLASS_TEXT)
            .input(dateString, dateString, new MaterialDialog.InputCallback() {
              @Override
              public void onInput(MaterialDialog dialog, CharSequence input) {
                recorderHandler.recordFileSave(input.toString());
              }
            }).show();

    dialog.getInputEditText().addTextChangedListener(new TextWatcher() {
      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.length() == 0) {
          dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        } else {
          dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
        }
      }

      @Override // 입력이 끝났을 때
      public void afterTextChanged(Editable s) {}

      @Override // 입력하기 전에
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    });
  }
}