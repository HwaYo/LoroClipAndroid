package com.loroclip;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loroclip.model.Record;

import java.util.List;

/**
 * Created by susu on 5/19/15.
 */
public class RecordListAdapter extends RecyclerView.Adapter<RecordListAdapter.ViewHolder> {
    public interface OnRecordSelectedListener {
        void onRecordSelected(Record record, View v);
        void onRecordLongSelected(Record record, View v);
    }

    public final static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        Record mRecord;
        TextView mTitle;
        TextView mDuration;
        OnRecordSelectedListener mListener;

        public ViewHolder(View view, OnRecordSelectedListener listener) {
            super(view);
            this.mListener = listener;
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
            mListener.onRecordSelected(mRecord, v);
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onRecordLongSelected(mRecord, v);
            return true;
        }
    }

    private final static String TAG = "RecordListAdapter";


    List<Record> mRecords;
    OnRecordSelectedListener mOnRecordSelectedListener;

    public RecordListAdapter(List<Record> recordList) {
        super();
        mRecords = recordList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate view and Attach Click Listeners
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view, mOnRecordSelectedListener);
    }

    public void setOnRecordSelectedListener(OnRecordSelectedListener mListener) {
        this.mOnRecordSelectedListener = mListener;
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
