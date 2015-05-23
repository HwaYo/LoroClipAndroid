package com.loroclip;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.loroclip.model.Bookmark;
import com.loroclip.record.RecordActivity;

/**
 * Created by susu on 5/20/15.
 */
public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {

    private final static String TAG = "BookmarkListAdapter";

    private Context mContext;
    private LayoutInflater mLayoutInflater;

    RecordActivity.BookmarkHandler bookmarkHandler;

    public BookmarkListAdapter(Context mContext, RecordActivity.BookmarkHandler bookmarkHandler) {
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.bookmarkHandler = bookmarkHandler;
    }

static class ViewHolder extends RecyclerView.ViewHolder {
        View viewHolder;

        public ViewHolder(View view) {
            super(view);
            viewHolder = view;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.bookmark_list_item, parent, false);
        view.setOnClickListener(bookmarkHandler);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Bookmark bookmark = bookmarkHandler.getBookmarkList().get(position);
        View view = holder.viewHolder;

        Drawable circle = mContext.getResources().getDrawable(R.drawable.circle);
        circle.setColorFilter(bookmark.getColor(), PorterDuff.Mode.MULTIPLY);

        ImageView img = (ImageView) view.findViewById(R.id.bookmark_image);
        img.setBackground(circle);

        TextView name = (TextView) view.findViewById(R.id.bookmark_name);
        name.setText(bookmark.getName());

    }

    @Override
    public int getItemCount() {
        return bookmarkHandler.getBookmarkList().size();
    }
}
