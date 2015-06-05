package com.loroclip;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.loroclip.adapter.BookmarkHistoryAdapter;
import com.loroclip.model.BookmarkHistory;
import com.loroclip.model.Record;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

/**
 * Created by susu on 5/23/15.
 * Fragment that contains BookmarkHistory and Bookmarks
 */
public class PlayerRecordHistoryFragment extends Fragment {

    public static final String ARG_RECORD_ID = "recordId";
    private Record mRecord;
    private List<BookmarkHistory> mBookmarkHistories;
    private BookmarkHistoryAdapter mBookmarkHistoryAdapter;
    private BookmarkHistoryAdapter.OnBookmarkHistorySelectedListener mCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        mRecord = Record.findById(Record.class, args.getLong(ARG_RECORD_ID));

        // Attach different Adapter for each page to this RecyclerView
        RecyclerView playRecycler = (RecyclerView) view.findViewById(R.id.recycler_play);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        playRecycler.setLayoutManager(manager);
        playRecycler.addItemDecoration(
                new HorizontalDividerItemDecoration
                        .Builder(getActivity())
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build()
        );

        mBookmarkHistories = mRecord.getBookmarkHistories();
        mBookmarkHistoryAdapter = new BookmarkHistoryAdapter(mBookmarkHistories, getActivity());
        mBookmarkHistoryAdapter.setOnBookmarkHistorySelectedListener(mCallback);
        playRecycler.setAdapter(mBookmarkHistoryAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCallback = (BookmarkHistoryAdapter.OnBookmarkHistorySelectedListener) activity;
    }

    public void notifyBookmarkHistoriesUpdate() {
        mBookmarkHistories.clear();
        mBookmarkHistories.addAll(mRecord.getBookmarkHistories());
        mBookmarkHistoryAdapter.notifyDataSetChanged();
    }
}
