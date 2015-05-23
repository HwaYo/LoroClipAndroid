
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
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.loroclip.BookmarkListAdapter;
import com.loroclip.R;
import com.loroclip.model.Bookmark;
import com.loroclip.model.BookmarkHistory;
import com.loroclip.model.FrameGains;
import com.loroclip.model.Record;
import com.loroclip.record.View.RecodWaveformView;
import com.loroclip.record.recorder.VorbisRecorder;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class RecordActivity extends ActionBarActivity {

  private final int READY_STATE = 0;
  private final int RECORDING_STATE = 1;
  private final int PAUSE_STATE = 2;

  private Toolbar mToolbar;
  private RecodWaveformView mWaveformView;

  private RecyclerView mBookmarkRecycler;
  private ImageView mRecordActionButton;
  private ImageView mRecordDoneButton;
  private ImageView mRecordTrashButton;

  private TimerHandler mTimerHandler;
  private RecorderHandler mRecorderHandler;
  private BookmarkHandler mBookmarkHandler;

  private LinearLayoutManager mLayoutManager;
  private BookmarkListAdapter mBookmarkListAdapter;

  private Record mRecord;
  private File mRecordFile;

  private int mRecordStatus;
  private boolean isSaved;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_new);

    mRecordStatus = READY_STATE;

    mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
    setSupportActionBar(mToolbar);

    mRecordDoneButton = (ImageView) findViewById(R.id.record_done_img);
    mRecordTrashButton = (ImageView) findViewById(R.id.record_trash_img);
    mRecordActionButton = (ImageView) findViewById(R.id.record_action_img);

    mRecordDoneButton.setEnabled(false);

    mLayoutManager = new LinearLayoutManager(this);
    mLayoutManager.setOrientation(OrientationHelper.VERTICAL);

    LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayViewTmp);
    mWaveformView = new RecodWaveformView(getBaseContext());
    displayLayout.addView(mWaveformView);


    addEventListener();
    mTimerHandler = new TimerHandler();
    mBookmarkHandler = new BookmarkHandler();
    mRecorderHandler = new RecorderHandler();

    mBookmarkListAdapter = new BookmarkListAdapter(this, mBookmarkHandler);

    mBookmarkRecycler = (RecyclerView) findViewById(R.id.bookmark_list);
    mBookmarkRecycler.setLayoutManager(mLayoutManager);
    mBookmarkRecycler.setAdapter(mBookmarkListAdapter);
    mBookmarkRecycler.addItemDecoration(
      new HorizontalDividerItemDecoration
        .Builder(this)
        .sizeResId(R.dimen.divider)
        .color(R.color.myGrayColor)
        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
        .build());

    isSaved = false;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();

    if(!isSaved) {
      notSaveFinish();
    }
  }

  private void addEventListener() {
    mRecordActionButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switch (mRecordStatus) {
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

    mRecordDoneButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pauseRecord();
        showSaveDialog();
      }
    });

    mRecordTrashButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDeleteDialog();
      }
    });
  }

  private void notSaveFinish() {
    if(mRecordStatus != READY_STATE) {
      stopRecord();
    }
    mBookmarkHandler.destroyBookmarAllkHistory();
    mRecorderHandler.deleteTempAudioRecordInformation();
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
          mRecorderHandler.save(input.toString());
          stopRecord();
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
      public void afterTextChanged(Editable s) {
      }

      @Override // 입력하기 전에
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }
    });

    dialog.setCanceledOnTouchOutside(false);
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

  private void startRecord() {
    //TODO 여기 일시정지 버튼으로 바뀌어야함
    mRecorderHandler.start();
    mTimerHandler.start();
    mRecordStatus = RECORDING_STATE;
    mRecordDoneButton.setEnabled(true);
  }
  private void pauseRecord() {
    //TODO 여기 일시정지 버튼으로 바뀌어야함
    mRecorderHandler.pause();
    mTimerHandler.pause();
    mRecordStatus = PAUSE_STATE;
  }
  private void restartRecord() {
    //TODO 여기 PLAY 버튼으로 바뀌어야함
    mRecorderHandler.restart();
    mTimerHandler.restart();
    mRecordStatus = RECORDING_STATE;
  }
  private void stopRecord() {
    mRecorderHandler.stop();
    mTimerHandler.stop();
    mRecordStatus = READY_STATE;
  }


  private class RecorderHandler {
    private VorbisRecorder loroclipRecorder;

    private final float LOROCLIP_AUDIO_QUALITY = 0.7f;
    private final int LOROCLIP_AUDIO_CHANNELS = 2;
    private final int LOROCLIP_AUDIO_SAMPLE_RATE = 44100;

    private final String AUDIO_OGG_EXTENSION = ".ogg";
    private final String LOROCLIP_TEMP_RECORDING_FILE_NAME = "loroclip_temp_recording_file";
    private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";;

    public void start() {
      File loroclipPath = new File(LOROCLIP_PATH);

      if(!loroclipPath.exists()) {
        loroclipPath.mkdirs();
      }

      mWaveformView.prepare();

      if (loroclipRecorder == null || loroclipRecorder.isStopped()) {
        mRecordFile = new File(LOROCLIP_PATH, UUID.randomUUID().toString() + AUDIO_OGG_EXTENSION);

        mRecord = new Record();
        mRecord.setLocalFile(mRecordFile);
        mRecord.save();

        if (loroclipRecorder == null) {
          loroclipRecorder = new VorbisRecorder(mRecordFile, mWaveformView);
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

    public void save(String title) {

      Log.e("aa", "save");
      if(mBookmarkHandler.getCurrentSelectedBookmarkHistory() != null) {
        mBookmarkHandler.finish();
      }

      final Handler handler = new Handler();
      mRecord.setTitle(title);
      mRecord.save();

      FrameGains fg = new FrameGains();
      fg.setFrames(mWaveformView.getJsonArray().toString());
      fg.setRecord(mRecord);
      fg.save();



      handler.post(new Runnable() {
        @Override
        public void run() {
          Toast.makeText(RecordActivity.this, "저장되었습니다.", Toast.LENGTH_SHORT).show();
        }
      });

      Intent intent = new Intent();
      intent.putExtra("record_id", mRecord.getId());
      setResult(Activity.RESULT_OK, intent);
      isSaved = true;
      Log.e("aa", "save finish");
      finish();
    }

    public void deleteTempAudioRecordInformation() {
      mRecord.delete();

      if(mRecordFile.exists()){
        mRecordFile.deleteOnExit();
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

    public float getTime() {
      return (float)(SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
    }
  }

  public class BookmarkHandler implements View.OnClickListener {

    private List<Bookmark> bookmarkList;
    private BookmarkHistory currentSelectedBookmarkHistory;

    public BookmarkHandler() {
      this.currentSelectedBookmarkHistory = null;
      this.bookmarkList = Bookmark.listExists(Bookmark.class);
    }
    @Override
    public void onClick(View v) {
      Bookmark selectedBookmark = bookmarkList.get(findPosition(v));

      if(mRecord == null) { return; }

      if(currentSelectedBookmarkHistory == null) {
        saveStartBookmarkHistory(selectedBookmark);
      } else if(currentSelectedBookmarkHistory.getBookmark().getId() == selectedBookmark.getId()) {
        saveEndBookmarkHistory();
      } else {
        saveEndBookmarkHistory();
        saveStartBookmarkHistory(selectedBookmark);
      }
    }

    private int findPosition ( View v ) {
      return mBookmarkRecycler.getChildLayoutPosition(v);
    }

    public List<Bookmark> getBookmarkList() {
      return bookmarkList;
    }

    private void saveStartBookmarkHistory(Bookmark selectedBookmark) {
      currentSelectedBookmarkHistory = new BookmarkHistory(mRecord, selectedBookmark);
      currentSelectedBookmarkHistory.setStart(mTimerHandler.getTime());
      mWaveformView.setCurrentSelectedBookmark(selectedBookmark);
    }

    private void saveEndBookmarkHistory() {
      currentSelectedBookmarkHistory.setEnd(mTimerHandler.getTime());
      currentSelectedBookmarkHistory.save();
      currentSelectedBookmarkHistory = null;
      mWaveformView.setCurrentRelaseBookmark();
    }

    public void finish() {
      Log.e("aa", "finish");
      currentSelectedBookmarkHistory.setEnd(mTimerHandler.getTime());
      currentSelectedBookmarkHistory.save();
    }

    public BookmarkHistory getCurrentSelectedBookmarkHistory() {
      return currentSelectedBookmarkHistory;
    }

    public void destroyBookmarAllkHistory() {
      List<BookmarkHistory> histories = mRecord.getBookmarkHistories();
      for (BookmarkHistory history : histories) {
        history.delete();
      }
    }
  }
}