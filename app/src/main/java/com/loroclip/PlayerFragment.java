package com.loroclip;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.loroclip.adapter.BookmarkHistoryAdapter;
import com.ogaclejapan.smarttablayout.utils.v4.FragmentPagerItem;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

/**
 * Created by susu on 5/23/15.
 * Fragment that contains BookmarkHistory and Bookmarks
 */
public class PlayerFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        int position = FragmentPagerItem.getPosition(getArguments());

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

        if ( position == 0 ) {
            // Bookmark History View
            BookmarkHistoryAdapter historyAdapter;

            historyAdapter = new BookmarkHistoryAdapter(getActivity(),playRecycler);
            historyAdapter.setCustomOnClickListener(new BookmarkHistoryOnClickListener(historyAdapter));

            playRecycler.setAdapter(historyAdapter);
        } else {
            // Bookmark List
            BookmarkListAdapter listAdapter;
            listAdapter = new BookmarkListAdapter(getActivity(),playRecycler);
            playRecycler.setAdapter(listAdapter);
        }

    }

}
