
package com.loroclip.record;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import android.view.Menu;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.loroclip.record.View.RecordWaveformView;
import com.loroclip.record.recorder.VorbisRecorder;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

public class RecordActivity extends ActionBarActivity {

  private final int READY_STATE = 0;
  private final int RECORDING_STATE = 1;
  private final int PAUSE_STATE = 2;


  private RecordWaveformView mWaveformView;

  private RecyclerView mBookmarkRecycler;
  private ImageView mRecordActionButton;
  private ImageView mRecordDoneButton;
  private ImageView mRecordTrashButton;

  private TimerHandler mTimerHandler;
  private RecorderHandler mRecorderHandler;
  private BookmarkHandler mBookmarkHandler;

  private Record mRecord;
  private List<Bookmark> mBookmarkList;
  private File mRecordFile;

  private int mRecordStatus;
  private boolean isSaved;

  private Animation fadeIn;
  private Animation fadeOut;

  private Toolbar mToolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_record_new);

    uiSetting();
    initializeSetting();
    handlerSetting();
    waveformViewSetting();
    bookmarkListAdapterSetting();
    animationSetting();
    addEventListener();


  }
  private void uiSetting() {

    // Android L Style Title Bar
    mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
    setSupportActionBar(mToolbar);
    getSupportActionBar().setTitle(R.string.new_record);

    mRecordDoneButton = (ImageView) findViewById(R.id.record_done_img);
    mRecordTrashButton = (ImageView) findViewById(R.id.record_trash_img);
    mRecordActionButton = (ImageView) findViewById(R.id.record_action_img);
  }

  private void initializeSetting() {
    mBookmarkList = Bookmark.listExists(Bookmark.class, "created_at ASC");
    mRecordStatus = READY_STATE;
    isSaved = false;
    mRecordDoneButton.setEnabled(false);
  }

  private void handlerSetting() {
    mTimerHandler = new TimerHandler();
    mBookmarkHandler = new BookmarkHandler();
    mRecorderHandler = new RecorderHandler();
  }

  private void waveformViewSetting() {
    LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayViewTmp);
    mWaveformView = new RecordWaveformView(getBaseContext());
    displayLayout.addView(mWaveformView);
  }

  private void bookmarkListAdapterSetting() {
    LinearLayoutManager layoutManager;
    BookmarkListAdapter bookmarkListAdapter;

    layoutManager = new LinearLayoutManager(this);
    layoutManager.setOrientation(OrientationHelper.VERTICAL);

    bookmarkListAdapter = new BookmarkListAdapter(mBookmarkList);
    bookmarkListAdapter.setOnBookmarkSelectedListener(mBookmarkHandler);

    mBookmarkRecycler = (RecyclerView) findViewById(R.id.bookmark_list);
    mBookmarkRecycler.setLayoutManager(layoutManager);
    mBookmarkRecycler.setAdapter(bookmarkListAdapter);
    mBookmarkRecycler.addItemDecoration(
            new HorizontalDividerItemDecoration
                    .Builder(this)
                    .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                    .build());
  }

  private void animationSetting() {
    fadeOut = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.fade_out);
    fadeIn = AnimationUtils.loadAnimation(RecordActivity.this, R.anim.fade_in);

    fadeOut.setAnimationListener(new Animation.AnimationListener() {
      @Override
      public void onAnimationStart(Animation animation) {
      }

      @Override
      public void onAnimationEnd(Animation animation) {
        changeRecordingButton();
        mRecordActionButton.startAnimation(fadeIn);
      }

      @Override
      public void onAnimationRepeat(Animation animation) {
      }
    });
  }


  private void changeRecordingButton() {
    if ( mRecordStatus == RECORDING_STATE ) {
      mRecordActionButton.setImageResource(R.drawable.record);
    } else {
      mRecordActionButton.setImageResource(R.drawable.pause);
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    return super.onCreateOptionsMenu(menu);
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
    if(mRecordFile != null && mRecordFile.exists()){
      mRecordFile.deleteOnExit();
    }
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
          mBookmarkHandler.save();
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
    changeRecordingButton();
    mRecorderHandler.start();
    mTimerHandler.start();
    mRecordStatus = RECORDING_STATE;
    mRecordDoneButton.setEnabled(true);
  }
  private void pauseRecord() {
    changeRecordingButton();
    mRecorderHandler.pause();
    mTimerHandler.pause();
    mRecordStatus = PAUSE_STATE;
  }
  private void restartRecord() {
    changeRecordingButton();
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
    private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Android/data/com.loroclip/files/";

    public void start() {
      File loroclipPath = new File(LOROCLIP_PATH);

      if(!loroclipPath.exists()) {
        loroclipPath.mkdirs();
      }

      mWaveformView.prepare();

      if (loroclipRecorder == null || loroclipRecorder.isStopped()) {
        mRecordFile = new File(LOROCLIP_PATH, UUID.randomUUID().toString() + AUDIO_OGG_EXTENSION);
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

      final Handler handler = new Handler();

      mRecord = new Record();
      mRecord.setLocalFile(mRecordFile);
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
      finish();
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

  public class BookmarkHandler implements BookmarkListAdapter.OnBookmarkSelectedListener {
    private List<BookmarkHistoryInformation> mBookmarkHistoryInformationList;
    private BookmarkHistoryInformation mBookmarkHistoryInformation;
    private View currentBookmarkView;

    public BookmarkHandler() {
      this.mBookmarkHistoryInformation = null;
      this.mBookmarkHistoryInformationList = new ArrayList<>();
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark, View v) {
      if(mRecordStatus != RECORDING_STATE) { return; }

      if(mBookmarkHistoryInformation == null) {
        saveStartBookmarkHistory(bookmark, v);
      } else if(mBookmarkHistoryInformation.getBookmark().getId() == bookmark.getId()) {
        saveEndBookmarkHistory();
      } else {
        saveEndBookmarkHistory();
        saveStartBookmarkHistory(bookmark, v);
      }
    }

    private void saveStartBookmarkHistory(Bookmark selectedBookmark, View v) {
      mBookmarkHistoryInformation = new BookmarkHistoryInformation(selectedBookmark, mTimerHandler.getTime());
      mWaveformView.setCurrentSelectedBookmark(selectedBookmark);
      currentBookmarkView = v;
      currentBookmarkView.setBackgroundColor(selectedBookmark.getColor());
    }

    private void saveEndBookmarkHistory() {
      mBookmarkHistoryInformation.setEndTime(mTimerHandler.getTime());
      mBookmarkHistoryInformationList.add(mBookmarkHistoryInformation);
      mBookmarkHistoryInformation = null;
      mWaveformView.setCurrentRelaseBookmark();
      currentBookmarkView.setBackgroundColor(Color.WHITE);

    }

    public void save() {
      if(mBookmarkHistoryInformation != null) {
        mBookmarkHistoryInformation.setEndTime(mTimerHandler.getTime());
        mBookmarkHistoryInformationList.add(mBookmarkHistoryInformation);
      }

      for(BookmarkHistoryInformation item : mBookmarkHistoryInformationList) {
        BookmarkHistory bookmarkHistory = new BookmarkHistory();
        bookmarkHistory.setRecord(mRecord);
        bookmarkHistory.setBookmark(item.getBookmark());
        bookmarkHistory.setStart(item.getStartTime());
        bookmarkHistory.setEnd(item.getEndTime());
        bookmarkHistory.save();
      }
    }

    private class BookmarkHistoryInformation {
      float startTime, endTime;
      Bookmark bookmark;

      public BookmarkHistoryInformation(Bookmark bookmark, float startTime) {
        this.startTime = startTime;
        this.bookmark = bookmark;
      }

      public void setEndTime(float endTime) {
        this.endTime = endTime;
      }

      public float getStartTime() {
        return startTime;
      }

      public float getEndTime() {
        return endTime;
      }

      public Bookmark getBookmark() {
        return bookmark;
      }
    }
  }
}