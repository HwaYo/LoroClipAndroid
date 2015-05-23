package com.loroclip;

import android.content.Intent;
import android.graphics.Color;
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


public class MainActivity extends ActionBarActivity {

    private static final String TAG = "RecordListView";

    private static final int REQUEST_CODE_NEW = 0;

    private Toolbar mToolbar;
    RecordListAdapter recordListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_material_list);

        // Android L Style Title Bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(mToolbar);

        RecyclerView recordList = (RecyclerView)findViewById(R.id.record_list);

        // Change this Adapter to fit LoroClip
        recordListAdapter = new RecordListAdapter(this, recordList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(OrientationHelper.VERTICAL);

//        Paint border = new Paint();
//        border.setStrokeWidth(0.1f);
//        border.setColor(Color.GRAY);

        recordList.setLayoutManager(manager);
        recordList.setAdapter(recordListAdapter);
        recordList.addItemDecoration(
                new HorizontalDividerItemDecoration
                        .Builder(this)
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build()
        );
//        recordList.addItemDecoration(
//                new HorizontalDividerItemDecoration
//                        .Builder(this)
//                        .sizeResId(R.dimen.divider)
//                        .color(Color.GRAY)
//                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
//                        .build());

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
                recordListAdapter.addRecord(record);
            }
        }

        recordListAdapter.notifyDataSetChanged();

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
                // TODO
                // Sync with Server
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
