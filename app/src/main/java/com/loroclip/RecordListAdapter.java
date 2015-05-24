package com.loroclip;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
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

import java.io.File;
import java.util.List;
import java.util.UUID;

/**
 * Created by susu on 5/19/15.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    public final static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final int CMD_RENAME = 0;
        private final int CMD_DELETE = 1;

        RecordListAdapter mAdapter;
        Record mRecord;
        TextView mTitle;
        TextView mDuration;

        public ViewHolder(RecordListAdapter adapter, View view) {
            super(view);
            this.mAdapter = adapter;
            this.mTitle = (TextView) view.findViewById(R.id.list_item_title);
            this.mDuration = (TextView) view.findViewById(R.id.list_item_time);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        public void bind(Record record) {
            mRecord = record;
            mTitle.setText(mRecord.getTitle());
            mDuration.setText("-");
        }

        @Override
        public void onClick(View v) {
            final Record record = mRecord;
            final Context context = v.getContext();

            if (record.getLocalFilePath() == null) {
                String filename = UUID.randomUUID().toString();
                final String LOROCLIP_PATH = Environment.getExternalStorageDirectory().toString() + "/Loroclip/";
                final String AUDIO_OGG_EXTENSION = ".ogg";

                final File recordFile = new File(LOROCLIP_PATH, filename + AUDIO_OGG_EXTENSION);

                new AlertDialog.Builder(context)
                        .setTitle("Record file not found")
                        .setMessage("File Download Required :")
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final ProgressDialog progressDialog = new ProgressDialog(context);
                                progressDialog.setMessage("Downloading..");
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                progressDialog.setMax(100);
                                progressDialog.show();

                                Ion.with(context)
                                        .load(record.getRemoteFilePath())
                                        .progressDialog(progressDialog)
                                        .progress(new ProgressCallback() {
                                            @Override
                                            public void onProgress(long downloaded, long total) {
                                                progressDialog.setProgress((int)(downloaded * 100 / total));
                                            }
                                        })
                                        .write(recordFile)
                                        .setCallback(new FutureCallback<File>() {
                                            @Override
                                            public void onCompleted(Exception e, File result) {
                                                progressDialog.dismiss();

                                                record.setLocalFile(result);
                                                record.save();

                                                Intent intent = new Intent(context, LoroClipEditActivity.class);
                                                intent.putExtra("record_id", record.getId());
                                                context.startActivity(intent);
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
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

        @Override
        public boolean onLongClick(View v) {
            final Context context = v.getContext();

            new MaterialDialog.Builder(context)
                    .title(R.string.edit_record)
                    .items(R.array.record_options)
                    .itemsCallback(new MaterialDialog.ListCallback() {
                        @Override
                        public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                            switch (which) {
                                case CMD_RENAME:
                                    showChangeTitleDialog(context, mRecord);
                                    break;
                                case CMD_DELETE:
                                    showDeleteDialog(context, mRecord);
                                    break;
                                default:
                            }
                        }
                    })
                    .show();

            return true;
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
                public void afterTextChanged(Editable s) {}

                @Override // 입력하기 전에
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            });

            dialog.getInputEditText().setSelection(dialog.getInputEditText().length());
        }

        private void changeTitle(Context context, Record record, String newTitle) {
            if (record == null) { return; }

            record.setTitle(newTitle);
            record.save();

            mAdapter.notifyItemChanged(getLayoutPosition());

            showToast(context, "변경되었습니다.");
        }

        private void showDeleteDialog(final Context context, final Record record) {

            new MaterialDialog.Builder(context)
                    .title(R.string.delete_audio)
                    .content(R.string.delete_audio_confirm)
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

        private void deleteRecord(Context context, Record record) {
            record.delete();
            mAdapter.notifyItemRemoved(getLayoutPosition());
            showToast(context, "삭제되었습니다.");
        }

        private void showToast(Context context, String msg) {
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }

    }

    private final static String TAG = "RecordListAdapter";


    List<Record> mRecords;

    public RecordListAdapter(List<Record> recordList) {
        super();
        mRecords = recordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view and Attach Click Listeners
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(RecordListAdapter.ViewHolder holder, int position) {
        holder.bind(mRecords.get(position));
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    public void addRecord(Record record) {
        mRecords.add(record);
    }
}
