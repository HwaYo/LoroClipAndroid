package com.loroclip;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.loroclip.model.Record;
import com.loroclip.record.RecordActivity;
import com.loroclip.util.Util;
import com.melnykov.fab.FloatingActionButton;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import net.hockeyapp.android.CrashManager;
import net.hockeyapp.android.UpdateManager;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class MainActivity extends ActionBarActivity implements RecordListAdapter.OnRecordSelectedListener {

    private static final String TAG = "RecordListView";

    private static final int REQUEST_CODE_NEW = 0;
    private final int CMD_RENAME = 0;
    private final int CMD_DELETE_RECORD = 1;
    private final int CMD_DELETE_FILE = 2;

    private List<Record> mRecords;
    private Toolbar mToolbar;
    RecordListAdapter mRecordListAdapter;

    private static boolean mSyncing = false;

    private static Typeface mTypeface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_material_list);
        setSyncAutomatic();

        // Android L Style Title Bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mTypeface = Typeface.createFromAsset(getAssets(), "fonts/Raleway-Regular.ttf");
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        Util.setGlobalFont(root, mTypeface);

        // Floating Button on Bottom Right Corner
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent i = new Intent(MainActivity.this, RecordActivity.class);
                    startActivityForResult(i, REQUEST_CODE_NEW);
                } catch (Exception e) {
                    Log.e("LoroClip", "Couldn't start editor");
                }
            }
        });

        checkForUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        UpdateManager.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mRecords = Record.listExists(Record.class);

        mRecordListAdapter = new RecordListAdapter(mRecords, this, (TextView)findViewById(R.id.emptyListText)  );
        mRecordListAdapter.setOnRecordSelectedListener(this);

        RecyclerView recordListView = (RecyclerView)findViewById(R.id.record_list);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(OrientationHelper.VERTICAL);
        recordListView.setLayoutManager(manager);
        recordListView.setAdapter(mRecordListAdapter);
        recordListView.addItemDecoration(
                new HorizontalDividerItemDecoration
                        .Builder(this)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build()
        );

        checkForCrashes();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_material_list, menu);

        return true;
    }

    private CharSequence wrapInSpan(CharSequence value) {
        SpannableStringBuilder sb = new SpannableStringBuilder(value);
        sb.setSpan(mTypeface, 0, value.length(), 0);
        return sb;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
        if (resultCode != RESULT_OK) {
            return;
        }

        long recordId = dataIntent.getLongExtra("record_id", 0);
        if (recordId != 0) {
            Record record = Record.findById(Record.class, recordId);
            if (record != null) {
                mRecords.add(0, record);
            }
        }
        mRecordListAdapter.notifyDataSetChanged();

        setResult(RESULT_OK, dataIntent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            /* Do something like showing SettingsActivity */
            case R.id.action_settings :
                break;
            case R.id.action_sync :
                ConnectivityManager connManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo wiki = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

                if (wiki.isConnected()){
                    requestSync();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.need_wifi, Toast.LENGTH_SHORT).show();
                }

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSyncAutomatic() {
        final Account account = LoroClipAccount.getInstance().getPrimaryAccount(this);
        if (account != null) {

//          ContentResolver.setIsSyncable(account, LoroClipAccount.CONTENT_AUTHORITY, 1);
            ContentResolver.setSyncAutomatically(account, LoroClipAccount.CONTENT_AUTHORITY, true);
            ContentResolver.addStatusChangeListener(
                    ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE | ContentResolver.SYNC_OBSERVER_TYPE_PENDING,
                    new SyncStatusObserver() {
                        @Override
                        public void onStatusChanged(int which) {
                            if (which == ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE) {
                                if (ContentResolver.isSyncActive(account, LoroClipAccount.CONTENT_AUTHORITY)) {
                                    // Nothing to do
                                } else {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (!mSyncing) {
                                                return;
                                            }

                                            mSyncing = false;
                                            mRecords.clear();
                                            mRecords.addAll(Record.listExists(Record.class));
                                            mRecordListAdapter.notifyDataSetChanged();

                                            Toast.makeText(getApplicationContext(), R.string.Synced, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }
                        }
                    }
            );
        }
    }

    private void requestSync() {
        if (mSyncing) {
            return;
        }

        Account primaryAccount = LoroClipAccount.getInstance().getPrimaryAccount(this);
        if (primaryAccount == null) {
            return;
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(primaryAccount, LoroClipAccount.CONTENT_AUTHORITY, settingsBundle);

        mSyncing = true;
    }

    @Override
    public void onRecordSelected(final Record record, View v) {
        final Context context = this;

        if (record.getLocalFilePath() == null) {
          showDownloadRecordDialog(context, record);
        } else {
            try {
                Intent intent = new Intent(context, LoroClipEditActivity.class);
                intent.putExtra("record_id", record.getId());
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Couldn't start editor activity");
            }
        }
    }

    private void showDownloadRecordDialog(final Context context, final Record record) {
        final File LOROCLIP_PATH = getExternalFilesDir(null);
        String filename = UUID.randomUUID().toString();
        final String AUDIO_OGG_EXTENSION = ".ogg";

        final File recordFile = new File(LOROCLIP_PATH, filename + AUDIO_OGG_EXTENSION);

        new MaterialDialog.Builder(context)
            .title(R.string.record_not_found)
            .content(R.string.file_download_required)
            .callback(new MaterialDialog.ButtonCallback() {
            @Override
            public void onPositive(MaterialDialog dialog) {
                final ProgressDialog progressDialog = new ProgressDialog(context);
                progressDialog.setMessage(getString(R.string.downloading));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setMax(100);
                progressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            Ion.getDefault(context).cancelAll();
                        }
                        return true;
                    }
                });
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                Ion.with(context)
                    .load(record.getRemoteFilePath())
                    .progressDialog(progressDialog)
                    .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {
                        progressDialog.setProgress((int) (downloaded * 100 / total));
                    }
                })
                .write(recordFile)
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        progressDialog.dismiss();
                        if (result != null) {
                            record.setLocalFile(result);
                            record.save();

                            Intent intent = new Intent(context, LoroClipEditActivity.class);
                            intent.putExtra("record_id", record.getId());
                            context.startActivity(intent);
                        }
                    }
                });
            }
        })
        .positiveText(R.string.Download)
        .negativeText(R.string.cancel)
        .show();
    }

    @Override
    public void onRecordLongSelected(final Record record, View v) {
        final Context context = this;

        new MaterialDialog.Builder(context)
                .title(R.string.edit_record)
                .items(R.array.record_options)
                .itemsCallback(new MaterialDialog.ListCallback() {
                  @Override
                  public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                    switch (which) {
                      case CMD_RENAME:
                        showChangeTitleDialog(context, record);
                        break;
                      case CMD_DELETE_RECORD:
                        showDeleteRecordDialog(context, record);
                        break;
                      case CMD_DELETE_FILE:
                        if (record.getSyncedAt().equals(new Date(0))) {
                          showToast(context, R.string.not_synced);
                        } else {
                          showDeleteFileDialog(context, record);
                        }
                        break;
                      default:
                    }
                  }
                })
                .show();
    }

    private void showChangeTitleDialog(final Context context, final Record record) {
        // show a dialog to set filename
        final MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.edit_name)
                .content(R.string.set_record_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(record.getTitle(), record.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        changeTitle(context, record, input.toString());
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
            public void afterTextChanged(Editable s) {
            }

            @Override // 입력하기 전에
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
        });

        dialog.getInputEditText().setSelection(dialog.getInputEditText().length());
    }

    private void changeTitle(Context context, Record record, String newTitle) {
        if (record == null) { return; }

        record.setTitle(newTitle);
        record.save();

        mRecordListAdapter.notifyDataSetChanged();
        showToast(context, R.string.changed);
    }

    private void showDeleteRecordDialog(final Context context, final Record record) {

        new MaterialDialog.Builder(context)
                .title(R.string.delete_record)
                .content(R.string.delete_record_confirm)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteRecord(context, record);
                    }
                })
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .show();
    }

    private void showDeleteFileDialog(final Context context, final Record record) {

        new MaterialDialog.Builder(context)
            .title(R.string.delete_file)
            .content(R.string.delete_file_confirm)
            .callback(new MaterialDialog.ButtonCallback() {
                @Override
                public void onPositive(MaterialDialog dialog) {
                    deleteFile(context, record);
                }
            })
            .positiveText(R.string.delete)
            .negativeText(R.string.cancel)
            .show();
    }

    private void deleteFile(Context context, Record record) {
        File file = record.getLocalFile();
        record.setLocalFilePath(null);
        record.save();
        if (file.exists()) {
            file.delete();
            showToast(context, R.string.deleted);
        } else {
            showToast(context, R.string.file_not_exist);
        }

    }

    private void deleteRecord(Context context, Record record) {
        mRecords.remove(record);
        record.delete();
        mRecordListAdapter.notifyDataSetChanged();
        showToast(context, R.string.deleted);
    }

    private void showToast(Context context, int msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }


    private void checkForCrashes() {
        CrashManager.register(this, "f584704a2fe6025c315ace5dee6d7b9f");
    }

    private void checkForUpdates() {
        
    }

    public static void setmSyncing(boolean mSyncing) {
        MainActivity.mSyncing = mSyncing;
    }
}
