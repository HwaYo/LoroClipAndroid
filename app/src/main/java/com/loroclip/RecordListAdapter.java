package com.loroclip;

import android.content.Context;
import android.content.Intent;
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
import com.loroclip.model.Record;

import java.util.List;

/**
 * Created by susu on 5/19/15.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    private final static String TAG = "RecordListAdpater";

    private final int CMD_RENAME = 0;
    private final int CMD_DELETE = 1;

    List<Record> mRecords;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;


    public RecordListAdapter( Context context, RecyclerView recyclerView ) {
        super();
        mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
        mRecords = Record.listExists(Record.class);
        mRecyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view and Attach Click Listeners
        View view = mLayoutInflater.inflate(R.layout.list_item, parent, false);
        view.setOnClickListener(new RecyclerOnClickListener());
        view.setOnLongClickListener(new RecyclerOnLongClickListener());

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView title = (TextView) holder.viewHolder.findViewById(R.id.list_item_title);
        title.setText(mRecords.get(position).getTitle());

        // TODO 시간을 넣어야 한다.
        TextView length = (TextView) holder.viewHolder.findViewById(R.id.list_item_time);
        length.setText(mRecords.get(position).getTitle());
    }

    @Override
    public int getItemCount() {
        return mRecords.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            viewHolder = view;
        }

    }

    public class RecyclerOnClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Record record = mRecords.get(findPosition(v));
            try {
                Intent intent = new Intent(mContext, LoroClipEditActivity.class);
                intent.putExtra("record_id", record.getId());
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Couldn't start editor activity");
            }
        }
    }

    public class RecyclerOnLongClickListener implements View.OnLongClickListener {

        @Override
        public boolean onLongClick(View v) {
            showListDialog(findPosition(v));
            return true; // Need to return true to block OnClick
        }
    }

    // Simple List Dialog Popup
    public boolean showListDialog(int position){
        new MaterialDialog.Builder(mContext)
            .title(R.string.edit_record)
            .items(R.array.record_options)
            .itemsCallback(new MaterialDialogCallback(position))
            .show();
        return false;
    }

    public class MaterialDialogCallback implements MaterialDialog.ListCallback {
        private int itemPosition;

        public MaterialDialogCallback(int itemPosition) { this.itemPosition = itemPosition;}

        @Override
        public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence text) {
            switch (which) {
                case CMD_RENAME:
                    showChangeTitleDialog(itemPosition);
                    break;
                case CMD_DELETE:
                    showDeleteDialog(itemPosition);
                    break;
                default:
            }
        }
    }

    private int findPosition ( View v ) {
        return mRecyclerView.getChildLayoutPosition(v);
    }

    private void showChangeTitleDialog(int position) {
        final Record record = mRecords.get(position);

        // show a dialog to set filename
        final MaterialDialog dialog = new MaterialDialog.Builder(mContext)
                .title(R.string.edit_name)
                .content(R.string.set_record_name)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .input(record.getTitle(), record.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        changeTitle(record, input.toString());
                        notifyDataSetChanged();
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

    private void changeTitle(Record record, String newTitle) {
        if (record == null) { return; }

        record.setTitle(newTitle);
        record.save();

        showToast("변경되었습니다.");
    }

    private void showDeleteDialog(int position) {
        final Record record = mRecords.get(position);

        new MaterialDialog.Builder(mContext)
                .title(R.string.delete_audio)
                .content(R.string.delete_audio_confirm)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        deleteRecord(record);
                        notifyDataSetChanged();
                    }
                })
                .positiveText(R.string.delete)
                .negativeText(R.string.cancel)
                .show();
    }

    private void deleteRecord(Record record) {
        mRecords.remove(record);
        record.delete();

        showToast("삭제되었습니다.");
    }

    private void showToast( String msg ) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public void addRecord(Record record) {
        mRecords.add(record);
    }
}
