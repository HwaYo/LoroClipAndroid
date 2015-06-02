package com.loroclip;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.model.Bookmark;
import com.loroclip.util.Util;

import java.util.List;

/**
 * Created by susu on 5/20/15.
 */
public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {
    public interface OnBookmarkSelectedListener {
        void onBookmarkSelected(Bookmark bookmark, View v);
    }

    private final static String TAG = "BookmarkListAdapter";

    private List<Bookmark> mBookmarkList;
    private OnBookmarkSelectedListener mOnBookmarkSelectedListener;
    private Context mContext;

    public BookmarkListAdapter(List<Bookmark> bookmarkList, Context mContext) {
        this.mBookmarkList = bookmarkList;
        this.mContext = mContext;
    }

    public void setOnBookmarkSelectedListener(OnBookmarkSelectedListener listener) {
        this.mOnBookmarkSelectedListener = listener;
    }

    static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Bookmark mBookmark;
        Drawable mCircle;
        ImageView mImage;
        TextView mName;
        OnBookmarkSelectedListener mListener;

        public ViewHolder(View view, Drawable circle, OnBookmarkSelectedListener listener) {
            super(view);

            view.setOnClickListener(this);
            this.mCircle = circle;
            this.mImage = (ImageView) view.findViewById(R.id.bookmark_image);
            this.mName = (TextView) view.findViewById(R.id.bookmark_name);
            this.mListener = listener;
        }

        public void bind(Bookmark bookmark) {
            mBookmark = bookmark;
            mCircle.setColorFilter(bookmark.getColor(), PorterDuff.Mode.MULTIPLY);
            mImage.setBackground(mCircle);
            mName.setText(bookmark.getName());
        }

        @Override
        public void onClick(View v) {
            this.mListener.onBookmarkSelected(mBookmark, v);
        }

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.bookmark_list_item, parent, false);
        Drawable circle = context.getResources().getDrawable(R.drawable.circle);

        Typeface typeface = Typeface.createFromAsset(mContext.getAssets(), "fonts/Raleway-Regular.ttf");
        Util.setGlobalFont((ViewGroup)view,typeface);

        return new ViewHolder(view, circle, this.mOnBookmarkSelectedListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Bookmark bookmark = mBookmarkList.get(position);
        holder.bind(bookmark);
    }

    @Override
    public int getItemCount() {
        return mBookmarkList.size();
    }


}
