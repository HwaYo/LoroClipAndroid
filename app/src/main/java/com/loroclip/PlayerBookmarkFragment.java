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

import com.loroclip.model.Bookmark;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.List;

/**
 * Created by susu on 5/23/15.
 * Fragment that contains BookmarkHistory and Bookmarks
 */
public class PlayerBookmarkFragment extends Fragment {
    private RecyclerView playRecycler;

    public interface OnBookmarkSelectedListener {
        void onBookmarkSelected(Bookmark bookmark, View v);
    }

    private OnBookmarkSelectedListener mSelectedCallback;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        playRecycler = (RecyclerView) view.findViewById(R.id.recycler_play);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        playRecycler.setLayoutManager(manager);
        playRecycler.addItemDecoration(
                new HorizontalDividerItemDecoration
                        .Builder(getActivity())
                        .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build()
        );

        BookmarkListAdapter listAdapter;
        final List<Bookmark> bookmarkList = Bookmark.listExists(Bookmark.class, "created_at ASC");
        listAdapter = new BookmarkListAdapter(bookmarkList);
        listAdapter.setOnBookmarkSelectedListener(new BookmarkListAdapter.OnBookmarkSelectedListener() {
            @Override
            public void onBookmarkSelected(Bookmark bookmark, View v) {
                mSelectedCallback.onBookmarkSelected(bookmark, v);
            }
        });
        playRecycler.setAdapter(listAdapter);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mSelectedCallback = (OnBookmarkSelectedListener) activity;
    }
}
