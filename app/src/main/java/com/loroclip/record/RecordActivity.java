
package com.loroclip.record;

import android.app.Activity;
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
import android.view.View;
import android.widget.Chronometer;
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
import java.util.UUID;


public class RecordActivity extends ActionBarActivity {

  private final int READY_STATE = 0;
  private final int RECORDING_STATE = 1;
  private final int PAUSE_STATE = 2;


  private ImageView recordActionButton;
  private ImageView recordDoneButton;
  private ImageView recordTrashButton;

  private RecodWaveformView waveformView;

  private RecorderHandler recorderHandler;
  private TimerHandler timerHandler;

  private Toolbar mToolbar;

  private BookmarkListAdapter bookmarkListAdapter;
  private RecyclerView bookmarkRecycler;
  private LinearLayoutManager manager;

  private int recordStatus;

  private File mRecordFile;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_new);

    recordStatus = READY_STATE;

    mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
    setSupportActionBar(mToolbar);

    recordActionButton = (ImageView) findViewById(R.id.record_action_img);
    recordDoneButton = (ImageView) findViewById(R.id.record_done_img);
    recordDoneButton.setEnabled(false);
    recordTrashButton = (ImageView) findViewById(R.id.record_trash_img);


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
    stopRecord();
    recorderHandler.deleteTempAudioRecordFile();
  }

  private void addEventListener() {
    recordActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (recordStatus) {
          case READY_STATE:
            startRecord();
            break;
          case RECORDING_STATE:
            pauseRecord();
            break;
          case PAUSE_STATE:
            restartRecord();
            break;
        }
      }
    });

    recordDoneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pauseRecord();
        showSaveDialog();
      }
    });

    recordTrashButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDeleteDialog();
      }
    });
  }

  private void startRecord() {
    //TODO 여기 일시정지 버튼으로 바뀌어야함
    recorderHandler.start();
    timerHandler.start();
    recordStatus = RECORDING_STATE;
    recordDoneButton.setEnabled(true);
  }
  private void pauseRecord() {
    //TODO 여기 일시정지 버튼으로 바뀌어야함
    recorderHandler.pause();
    timerHandler.pause();
    recordStatus = PAUSE_STATE;
  }
  private void restartRecord() {
    //TODO 여기 PLAY 버튼으로 바뀌어야함
    recorderHandler.restart();
    timerHandler.restart();
    recordStatus = RECORDING_STATE;
  }
  private void stopRecord() {
    recorderHandler.stop();
    timerHandler.stop();
    recordStatus = READY_STATE;
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
        String filename = UUID.randomUUID().toString();
        mRecordFile = new File(LOROCLIP_PATH, filename + AUDIO_OGG_EXTENSION);

        //Create our recorder if necessary
        if (loroclipRecorder == null) {
          loroclipRecorder = new VorbisRecorder(mRecordFile, waveformView);
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

    public void saveRecord(String title) {
      final Handler handler = new Handler();
      Record record = new Record();
      record.setLocalFile(mRecordFile);
      record.setTitle(title);
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

  private void showDeleteDialog() {
    new MaterialDialog.Builder(this)
        .title(R.string.delete_audio)
        .content(R.string.delete_audio_confirm)
        .callback(new MaterialDialog.ButtonCallback() {
          @Override
          public void onPositive(MaterialDialog dialog) {
            finish();
          }
        })
        .positiveText(R.string.delete)
        .negativeText(R.string.cancel)
        .show()
        .setCanceledOnTouchOutside(false);
  }



  private void showSaveDialog() {
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    String dateString = format.format(Calendar.getInstance().getTime());

    // show a dialog to set filename
    final MaterialDialog dialog = new MaterialDialog.Builder(this)
                                      .title(R.string.edit_name)
                                      .content(R.string.set_record_name)
                                      .inputType(InputType.TYPE_CLASS_TEXT)
                                      .negativeText(R.string.cancel)
                                      .input(dateString, dateString, new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                          stopRecord();
                                          recorderHandler.saveRecord(input.toString());
                                        }
            })
            .show();

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

    dialog.setCanceledOnTouchOutside(false);
  }
}