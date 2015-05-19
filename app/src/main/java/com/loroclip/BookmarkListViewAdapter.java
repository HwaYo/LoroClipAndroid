package com.loroclip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.model.Bookmark;

import java.util.List;

/**
 * Created by minhyeok on 5/11/15.
 */
public class BookmarkListViewAdapter extends BaseAdapter {
    private List<Bookmark> mBookmarks;

    public BookmarkListViewAdapter() {
        mBookmarks = Bookmark.listAll(Bookmark.class);
    }

    @Override
    public int getCount() {
        return mBookmarks.size();
    }

    @Override
    public Object getItem(int i) {
        return mBookmarks.get(i);
    }

    @Override
    public long getItemId(int position) { return mBookmarks.get(position).getId(); }

    @Override
    public View getView(int pos, View view, ViewGroup parent) {
        Context context = parent.getContext();

        Bookmark bookmark = mBookmarks.get(pos);
        String currentBookmarkName = bookmark.getName();
        int currentBookmarkColor = bookmark.getColor();

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
