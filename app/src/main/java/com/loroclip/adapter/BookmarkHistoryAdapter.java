package com.loroclip.adapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.R;
import com.loroclip.model.Bookmark;
import com.loroclip.model.BookmarkHistory;

import java.util.List;

/**
 * Created by susu on 5/24/15.
 */
public class BookmarkHistoryAdapter extends RecyclerView.Adapter<BookmarkHistoryAdapter.ViewHolder> {
    public interface OnBookmarkHistorySelectedListener {
        void onBookmarkHistorySelected(BookmarkHistory history, View v, int position);
        void onBookmarkHistoryLongSelected(BookmarkHistory history, View v, int position);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        BookmarkHistory mHistory;
        ImageView mImage;
        Drawable mCircle;
        TextView mDurationText;
        TextView mBookmarkNameText;
        OnBookmarkHistorySelectedListener mListener;

        public ViewHolder(View view, Drawable circle, OnBookmarkHistorySelectedListener listener) {
            super(view);

            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
            this.mCircle = circle;
            this.mImage = (ImageView)view.findViewById(R.id.bookmark_history_image);
            this.mDurationText = (TextView)view.findViewById(R.id.bookmark_history_item_time);
            this.mBookmarkNameText = (TextView)view.findViewById(R.id.bookmark_history_item_name);
            this.mListener = listener;
        }

        public void bind(BookmarkHistory history) {
            mHistory = history;
            Bookmark bookmark = mHistory.getBookmark();
            mCircle.setColorFilter(bookmark.getColor(), PorterDuff.Mode.MULTIPLY);
            mImage.setBackground(mCircle);
            mDurationText.setText(formatSecond(mHistory.getStart()) + " - " + formatSecond(mHistory.getEnd()) );
            mBookmarkNameText.setText(bookmark.getName());
        }

        @Override
        public void onClick(View v) {
            mListener.onBookmarkHistorySelected(mHistory, v, getLayoutPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            mListener.onBookmarkHistoryLongSelected(mHistory, v, getLayoutPosition());
            return true;
        }
    }

    private String formatSecond(float fsec) {
        int sec = (int) fsec;
        String result = "";

        if ( sec >= 3600 ) {
            result += String.valueOf((sec / 3600)) + "h";
        }
        if ( sec >= 60 ) {
            result += String.valueOf(((sec % 3600)/60) ) + "m";
        }
        result += String.valueOf(sec % 60) + "s";
        
        return result;
    }

    private List<BookmarkHistory> mBookmarkHistoryList;
    private OnBookmarkHistorySelectedListener mOnBookmarkHistorySelectedListener;

    public BookmarkHistoryAdapter(List<BookmarkHistory> bookmarkHistoryList) {
        mBookmarkHistoryList = bookmarkHistoryList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.bookmark_history_item, parent, false);
        Drawable circle = context.getResources().getDrawable(R.drawable.circle);

        return new ViewHolder(view, circle, mOnBookmarkHistorySelectedListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        BookmarkHistory history = mBookmarkHistoryList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return mBookmarkHistoryList.size();
    }

    public void setOnBookmarkHistorySelectedListener(OnBookmarkHistorySelectedListener OnBookmarkHistorySelectedListener) {
        this.mOnBookmarkHistorySelectedListener = OnBookmarkHistorySelectedListener;
    }
}
