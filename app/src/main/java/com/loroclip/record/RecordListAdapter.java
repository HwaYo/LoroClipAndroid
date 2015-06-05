package com.loroclip.record;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.loroclip.model.Record;

import java.util.List;

/**
 * Created by minhyeok on 5/19/15.
 */
public class RecordListAdapter extends BaseAdapter{
    private List<Record> mRecords;
    private Context mContext;

    public RecordListAdapter(List<Record> records, Context mContext) {
        this.mRecords = records;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mRecords.size();
    }

    @Override
    public String getItem(int i) {
        return mRecords.get(i).getTitle();
    }

    @Override
    public long getItemId(int i) {
        return mRecords.get(i).getId();
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);

        TextView titleTextView = (TextView) view.findViewById(android.R.id.text1);
        titleTextView.setText(getItem(i));

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Raleway-Regular.ttf");
        titleTextView.setTypeface(typeface);

        return view;

    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }
}
