package com.loroclip;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.loroclip.adapter.BookmarkHistoryAdapter;
import com.loroclip.model.Bookmark;
import com.loroclip.model.BookmarkHistory;

/**
 * Created by susu on 5/23/15.
 */
public class BookmarkHistoryOnClickListener implements View.OnClickListener {

    private BookmarkHistoryAdapter mAdapter;

    public BookmarkHistoryOnClickListener(BookmarkHistoryAdapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public void onClick(View v) {

        BookmarkHistory bookmarkHistory = mAdapter.findBookmarkHistory(v);
        // TODO
        // Do something with bookmark history
        // for example, moving the player
        mAdapter.showToast("BookmarkHistory OnClickListener");

    }
}
