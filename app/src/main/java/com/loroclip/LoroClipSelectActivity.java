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
import android.net.Uri;
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

import com.loroclip.model.BookmarkHistory;
import com.loroclip.model.FrameGains;
import com.loroclip.model.Record;
import com.loroclip.record.RecordActivity;
import com.loroclip.record.RecordListAdapter;

import java.io.File;
import java.util.List;

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

        mAdapter = new RecordListAdapter();

        getListView().setAdapter(mAdapter);
        getListView().setItemsCanFocus(true);

        // Normal click - open the editor
        getListView().setOnItemClickListener(new OnItemClickListener() {public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
    protected void onActivityResult(int requestCode, int resultCode, Intent dataIntent) {
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        selectedFileName = getfileNameFromRowView(((AdapterView.AdapterContextMenuInfo) menuInfo).targetView.findViewById(android.R.id.text1));
        menu.add(0, CMD_RENAME, 0, R.string.context_menu_edit);
        menu.add(0, CMD_DELETE, 0, R.string.context_menu_delete);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CMD_RENAME:
                changeNameDialog();
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
            .setPositiveButton(R.string.delete_ok_button,new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    onDelete();
                }
            })
            .setNegativeButton(R.string.delete_cancel_button, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int whichButton) {}
            })
            .setCancelable(true)
            .show();
    }

    private void changeNameDialog() {
        final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
        EditText saveFile = (EditText)view.findViewById(R.id.filenameEditText);

        saveFile.setText(selectedFileName);
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
                    String filename = ((EditText) view.findViewById(R.id.filenameEditText)).getText().toString();
                    onChangeFileName(filename);
                    mAdapter.notifyDataSetChanged();
                }
            })
            .setNegativeButton("CANCEL", null)
            .create();;

        dialog.show();
        saveFile.setSelection(saveFile.getText().length());
        saveFile.requestFocus();
    }

    private void onChangeFileName(String newFileName) {

        // 파일이름 변경
        String fromFilePath = LOROCLIP_PATH + selectedFileName + AUDIO_OGG_EXTENSION;
        String newFilePath = LOROCLIP_PATH + newFileName + AUDIO_OGG_EXTENSION;
        File from = new File(fromFilePath);
        File to = new File(newFilePath);
        from.renameTo(to);


        //** DB 변경 **//
        // TODO 파일이름 변경 (파일 롱클릭 이름변경 승인 누를시 - FrameGain과 Bookmark도 변경해야 할듯)

        // Record
        Record r = Record.find(Record.class, "title = ?", selectedFileName).get(0);
        r.setTitle(newFileName);
        r.setFile(newFilePath);
        r.save();

        // FrameGain
        FrameGains fg = FrameGains.find(FrameGains.class, "record = ?", String.valueOf(r.getId())).get(0);
        fg.setRecord(r);
        fg.save();

        // BookMark
        List<BookmarkHistory> h = BookmarkHistory.find(BookmarkHistory.class, "filename = ?", fromFilePath);
        for(BookmarkHistory item : h) {
            item.setFilename(newFilePath);
            item.save();
        }

        final android.os.Handler handler = new android.os.Handler();

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(LoroClipSelectActivity.this, "변경되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onDelete() {
        if (!selectedFileName.isEmpty()) {
            if (new File(getFilePathFromFileName(selectedFileName)).delete()) {
                // TODO 디비지우는것 (파일 롱클릭 삭제누를시 - FrameGain과 Bookmark도 지워야 할듯)
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
