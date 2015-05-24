package com.loroclip.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loroclip.R;
import com.loroclip.model.BookmarkHistory;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by susu on 5/24/15.
 */
public class BookmarkHistoryAdapter extends RecyclerView.Adapter<BookmarkHistoryAdapter.ViewHolder> {

    private List<BookmarkHistory> bookmarkHistoryList;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;

    private View.OnClickListener customOnClickListener;
    private View.OnLongClickListener customOnLongClickListener;

    public BookmarkHistoryAdapter(Context mContext, RecyclerView mRecyclerView) {
        this.mContext = mContext;
        this.mRecyclerView = mRecyclerView;
        this.mLayoutInflater = LayoutInflater.from(mContext);

        // TODO
        // implement a initializing method for bookmarkHistoryList
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.bookmark_history_item, parent, false);

        // TODO
        // Do something with the view ( setting on click listeners )
        if ( customOnClickListener != null ) {
            // view.setOnClick  ...
        }
        if ( customOnLongClickListener != null ) {
            // view.setOnLongClick  ...
        }

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        // TODO Fix this
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        View viewHolder;

        public ViewHolder(View viewHolder) {
            super(viewHolder);
            this.viewHolder = viewHolder;
        }
    }

    private int findPosition ( View v ) {
        return mRecyclerView.getChildLayoutPosition(v);
    }

    public BookmarkHistory findBookmarkHistory ( View v ) {
        return bookmarkHistoryList.get( findPosition(v) );
    }

    public void showToast( String msg ) {
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }

    public void setCustomOnClickListener(View.OnClickListener customOnClickListener) {
        this.customOnClickListener = customOnClickListener;
    }

    public void setCustomOnLongClickListener(View.OnLongClickListener customOnLongClickListener) {
        this.customOnLongClickListener = customOnLongClickListener;
    }
}
