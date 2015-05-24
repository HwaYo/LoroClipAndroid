package com.loroclip;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.loroclip.model.Record;
import com.loroclip.record.RecordActivity;
import com.melnykov.fab.FloatingActionButton;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "RecordListView";

    private static final int REQUEST_CODE_NEW = 0;

    private List<Record> mRecords;
    private Toolbar mToolbar;
    RecordListAdapter mRecordListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list);

        final Account account = LoroClipAccount.getInstance().getPrimaryAccount(this);
        if (account != null) {
            ContentResolver.setIsSyncable(account, LoroClipAccount.CONTENT_AUTHORITY, 1);
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
                                            mRecords.clear();
                                            mRecords.addAll(Record.listExists(Record.class));
                                            mRecordListAdapter.notifyDataSetChanged();
                                        }
                                    });
                                }
                            }
                        }
                    }
            );
        }

        mRecords = Record.listExists(Record.class);

        // Android L Style Title Bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        mRecordListAdapter = new RecordListAdapter(mRecords);

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_material_list, menu);
        return true;
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
                mRecordListAdapter.addRecord(record);
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
                requestSync();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestSync() {
        Account primaryAccount = LoroClipAccount.getInstance().getPrimaryAccount(this);
        if (primaryAccount == null) {
            return;
        }

        Bundle settingsBundle = new Bundle();
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        settingsBundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        ContentResolver.requestSync(primaryAccount, LoroClipAccount.CONTENT_AUTHORITY, settingsBundle);
    }
}
