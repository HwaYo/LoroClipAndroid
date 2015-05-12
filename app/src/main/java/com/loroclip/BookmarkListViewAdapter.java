package com.loroclip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by minhyeok on 5/11/15.
 */
public class BookmarkListViewAdapter extends BaseAdapter {
    private ArrayList<String> bookmarkNameList;
    private BookmarkMap savedBookmarkMap;

    public BookmarkListViewAdapter() {
        savedBookmarkMap = new BookmarkMap();
        bookmarkNameList = new ArrayList<String>(Arrays.asList(savedBookmarkMap.keySet().toArray(new String[savedBookmarkMap.keySet().size()])));
    }

    @Override
    public int getCount() {
        return bookmarkNameList.size();
    }

    @Override
    public Object getItem(int i) {
        return bookmarkNameList.get(i);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        Context context = parent.getContext();
        String currentBookmarkName = bookmarkNameList.get(pos);
        int currentBookmarkColor = savedBookmarkMap.get(currentBookmarkName);

        if (view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.bookmark_listview_item, parent, false);

            TextView bookmarkName = (TextView) view.findViewById(R.id.bookmarkName);
            bookmarkName.setText(currentBookmarkName);

            ImageView bookmarkColor = (ImageView)view.findViewById(R.id.bookmarkColor);
            bookmarkColor.setBackgroundColor(currentBookmarkColor);


        }
        return view;
    }
}
