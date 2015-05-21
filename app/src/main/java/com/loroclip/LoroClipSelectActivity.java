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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loroclip.model.Record;
import com.loroclip.record.RecordActivity;
import com.loroclip.record.RecordListAdapter;

import java.util.List;

public class LoroClipSelectActivity extends ListActivity {
    private final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";;
    private final String AUDIO_OGG_EXTENSION = ".ogg";

    // Result codes
    private static final int REQUEST_CODE_NEW = 0;
    private static final int REQUEST_CODE_EDIT = 1;

    // Context menu
    private static final int CMD_RENAME = 4;
    private static final int CMD_DELETE = 5;

    private List<Record> mRecords;
    private RecordListAdapter mAdapter;

    private AlertDialog dialog;

    public LoroClipSelectActivity() {}

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

        mRecords = Record.listExists(Record.class);
        mAdapter = new RecordListAdapter(mRecords);

        getListView().setAdapter(mAdapter);
        getListView().setItemsCanFocus(true);

        // Normal click - open the editor
        getListView().setOnItemClickListener(new OnItemClickListener() {public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Record record = mRecords.get(position);
                startLoroClipEditor(record);
            }
        });

        // Long-press opens a context menu
        registerForContextMenu(getListView());
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private String getFilePathFromFileName(String fileName) {
        return LOROCLIP_PATH + fileName + AUDIO_OGG_EXTENSION;
    }


    private String getfileNameFromRowView(View view) {
        TextView tv = (TextView)view.findViewById(android.R.id.text1);
        return (String) tv.getText();
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
                mRecords.add(record);
            }
        }

        mAdapter.notifyDataSetChanged();

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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

//        selectedFileName = getfileNameFromRowView(((AdapterView.AdapterContextMenuInfo) menuInfo).targetView.findViewById(android.R.id.text1));
        menu.add(0, CMD_RENAME, 0, R.string.context_menu_edit);
        menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {
            case CMD_RENAME:
                changeTitleDialog(info.position);
                return true;
            case CMD_DELETE:
                confirmDelete(info.position);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void confirmDelete(int position) {
        final Record record = mRecords.get(position);

        new AlertDialog.Builder(LoroClipSelectActivity.this)
            .setTitle(R.string.delete_music)
            .setMessage(R.string.confirm_delete_loroclip)
            .setPositiveButton(R.string.delete_ok_button,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    onDelete(record);
                    mAdapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton(R.string.delete_cancel_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int whichButton) {}
            })
            .setCancelable(true)
            .show();
    }

    private void changeTitleDialog(int position) {
        final Record record = mRecords.get(position);

        final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
        EditText saveFile = (EditText)view.findViewById(R.id.filenameEditText);

        saveFile.setText(record.getTitle());
        saveFile.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override // 입력이 끝났을 때
            public void afterTextChanged(Editable s) {}

            @Override // 입력하기 전에
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });


        dialog = new AlertDialog.Builder(this)
            .setTitle("파일이름변경")
            .setView(view)
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String title = ((EditText) view.findViewById(R.id.filenameEditText)).getText().toString();
                    onChangeTitle(record, title);
                    mAdapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton("CANCEL", null)
            .create();;

        dialog.show();
        saveFile.setSelection(saveFile.getText().length());
        saveFile.requestFocus();
    }

    private void onChangeTitle(Record record, String newTitle) {
        if (record == null) {
            return;
        }

        record.setTitle(newTitle);
        record.save();

        final android.os.Handler handler = new android.os.Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoroClipSelectActivity.this, "변경되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDelete(Record record) {
        mRecords.remove(record);
        record.delete();
    }

    private void showFinalAlert(CharSequence message) {
        new AlertDialog.Builder(LoroClipSelectActivity.this)
        .setTitle(getResources().getText(R.string.alert_title_failure))
        .setMessage(message)
        .setPositiveButton(R.string.alert_ok_button,new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                finish();
            }
        })
        .setCancelable(false)
        .show();
    }

    private void onRecord() {
        try {
            Intent i = new Intent(LoroClipSelectActivity.this, RecordActivity.class);
            startActivityForResult(i, REQUEST_CODE_NEW);
        } catch (Exception e) {
            Log.e("LoroClip", "Couldn't start editor");
        }
    }

    private void startLoroClipEditor(Record record) {
        try {
            Intent intent = new Intent(this, LoroClipEditActivity.class);
            intent.putExtra("record_id", record.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e("LoroClip", "Couldn't start editor");
        }
    }
}
