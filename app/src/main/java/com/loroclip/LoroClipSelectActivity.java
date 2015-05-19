/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loroclip;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.loroclip.model.Record;
import com.loroclip.record.RecordActivity;
import com.loroclip.record.RecordListAdapter;
import com.loroclip.soundfile.SoundFile;

import org.w3c.dom.Text;

public class LoroClipSelectActivity extends ListActivity {
    private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";;
    private final String AUDIO_OGG_EXTENSION = ".ogg";

    // Result codes
    private static final int REQUEST_CODE_EDIT = 1;

    // Context menu
    private static final int CMD_RENAME = 4;
    private static final int CMD_DELETE = 5;
    private String selectedFileName;

    private RecordListAdapter mAdapter;


    public LoroClipSelectActivity() {
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
            showFinalAlert(getResources().getText(R.string.sdcard_readonly));
            return;
        }
        if (status.equals(Environment.MEDIA_SHARED)) {
            showFinalAlert(getResources().getText(R.string.sdcard_shared));
            return;
        }
        if (!status.equals(Environment.MEDIA_MOUNTED)) {
            showFinalAlert(getResources().getText(R.string.no_sdcard));
            return;
        }

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.media_select);

        mAdapter = new RecordListAdapter();

        getListView().setAdapter(mAdapter);
        getListView().setItemsCanFocus(true);

        // Normal click - open the editor
        getListView().setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view,
                                    int position,
                                    long id) {
                String fileName = getfileNameFromRowView(view);
                String filePath = getFilePathFromFileName(fileName);
                startLoroClipEditor(filePath);
            }
        });

        // Long-press opens a context menu
        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        mAdapter.notifyDataSetChanged();
    }

    private String getFilePathFromFileName(String fileName) {
        return LOROCLIP_PATH + fileName + AUDIO_OGG_EXTENSION;
    }

    private String getfileNameFromRowView(View view) {
        TextView tv = (TextView)view.findViewById(android.R.id.text1);
        return (String) tv.getText();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
            Intent dataIntent) {
        if (requestCode != REQUEST_CODE_EDIT) {
            return;
        }

        if (resultCode != RESULT_OK) {
            return;
        }

        setResult(RESULT_OK, dataIntent);
        //finish();  // TODO(nfaralli): why would we want to quit the app here?
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.select_options, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.action_record).setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_record:
            onRecord();
            return true;
        default:
            return false;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu,
            View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        selectedFileName = getfileNameFromRowView(((AdapterView.AdapterContextMenuInfo) menuInfo).targetView.findViewById(android.R.id.text1));
        menu.add(0, CMD_RENAME, 0, R.string.context_menu_edit);
        menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CMD_RENAME:
                // TODO: RENAME
                return true;
            case CMD_DELETE:
                confirmDelete();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(LoroClipSelectActivity.this)
                .setTitle(R.string.delete_music)
                .setMessage(R.string.confirm_delete_loroclip)
                .setPositiveButton(
                        R.string.delete_ok_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                onDelete();
                            }
                        })
                .setNegativeButton(
                        R.string.delete_cancel_button,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        })
                .setCancelable(true)
                .show();

    }

    private void onDelete() {
        if (!selectedFileName.isEmpty()) {
            if (new File(getFilePathFromFileName(selectedFileName)).delete()) {
                Record.deleteAll(Record.class, "title = ?", selectedFileName);
                mAdapter.notifyDataSetChanged();
            } else{
                showFinalAlert(getResources().getText(R.string.delete_failed));
            }
        }
    }

    private void showFinalAlert(CharSequence message) {
        new AlertDialog.Builder(LoroClipSelectActivity.this)
        .setTitle(getResources().getText(R.string.alert_title_failure))
        .setMessage(message)
        .setPositiveButton(
                R.string.alert_ok_button,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,
                            int whichButton) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void onRecord() {
        try {
            Intent i = new Intent(LoroClipSelectActivity.this, RecordActivity.class);
            startActivity(i);
        } catch (Exception e) {
            Log.e("LoroClip", "Couldn't start editor");
        }
    }

    private void startLoroClipEditor(String filePath) {
        try {
            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(filePath));
            intent.setClassName( "com.loroclip", "com.loroclip.LoroClipEditActivity");
            startActivityForResult(intent, REQUEST_CODE_EDIT);
        } catch (Exception e) {
            Log.e("LoroClip", "Couldn't start editor");
        }
    }
}
