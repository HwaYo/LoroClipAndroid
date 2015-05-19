package com.loroclip.record;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.R;
import com.loroclip.model.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by minhyeok on 5/19/15.
 */
public class RecordListAdapter extends BaseAdapter{
    private String[] titleArray;

    public RecordListAdapter() {
        refreshRecordList();
    }

    private void refreshRecordList() {
        List<Record> records = Record.listAll(Record.class);
        ArrayList<String> titles = new ArrayList<String>();
        for(int i=0; i < records.size(); i++) {
            titles.add(records.get(i).getTitle());
        }

        titleArray = new String[titles.size()];
        titles.toArray(titleArray);
    }

    @Override
    public int getCount() {
        return titleArray.length;
    }

    @Override
    public String getItem(int i) {
        return titleArray[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        String fileTitle = titleArray[i];

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(android.R.layout.simple_list_item_1, viewGroup, false);

        TextView titleTextView = (TextView) view.findViewById(android.R.id.text1);
        titleTextView.setText(fileTitle);

        return view;

    }

    @Override
    public void notifyDataSetChanged() {
        refreshRecordList();
        super.notifyDataSetChanged();
    }
}
