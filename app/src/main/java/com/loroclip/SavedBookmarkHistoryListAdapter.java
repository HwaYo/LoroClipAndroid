package com.loroclip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.model.BookmarkHistory;
import com.loroclip.util.Util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by minhyeok on 5/13/15.
 */
public class SavedBookmarkHistoryListAdapter extends BaseAdapter {
    private List<BookmarkHistory> savedBookmarkHistory;

    public SavedBookmarkHistoryListAdapter(String fileName) {
        savedBookmarkHistory = new BookmarkHistory().find(BookmarkHistory.class, "filename = ?", fileName);
        Collections.sort(savedBookmarkHistory, new Comparator<BookmarkHistory>(){

            @Override
            public int compare(BookmarkHistory bh1, BookmarkHistory bh2) {
                return bh1.getStartMiiliseconds() - bh2.getStartMiiliseconds();
            }
        });
    }

    @Override
    public int getCount() {
        return savedBookmarkHistory.size();
    }

    @Override
    public Object getItem(int i) {
        return savedBookmarkHistory.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, final ViewGroup viewGroup) {
        Context context = viewGroup.getContext();
        BookmarkHistory bookmarkHistory = savedBookmarkHistory.get(i);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.saved_bookmark_history_listview_item, viewGroup, false);

        TextView bookmarkName = (TextView)view.findViewById(R.id.bookmarkHistoryName);
        bookmarkName.setText(bookmarkHistory.getName());

        ImageView bookmarkColor = (ImageView)view.findViewById(R.id.bookmarkHistoryColor);
        bookmarkColor.setBackgroundColor(bookmarkHistory.getColor());

        TextView bookmarkStartTime = (TextView)view.findViewById(R.id.bookmarkStartTime);
        bookmarkStartTime.setText(Util.milliSecondsToMinutesStr(bookmarkHistory.getStartMiiliseconds()));

        return view;
    }

    public List<BookmarkHistory> getSavedBookmarkHistory() {
        return savedBookmarkHistory;
    }
}
