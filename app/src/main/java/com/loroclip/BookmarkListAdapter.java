package com.loroclip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.loroclip.model.Bookmark;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by susu on 5/20/15.
 */
public class BookmarkListAdapter extends RecyclerView.Adapter<BookmarkListAdapter.ViewHolder> {

    private final static String TAG = "BookmarkListAdapter";

    private List<Bookmark> bookmarkList;

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private RecyclerView mRecyclerView;
    private View.OnClickListener customOnClickListener;
    private View.OnLongClickListener customOnLongClickListener;

    public BookmarkListAdapter(Context mContext, RecyclerView mRecyclerView) {
        this.bookmarkList = getDataForListView();
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mRecyclerView = mRecyclerView;
        this.customOnClickListener = new BookmarkOnClickListener();
        this.customOnLongClickListener = null;
    }

    public BookmarkListAdapter(Context mContext, RecyclerView mRecyclerView, View.OnClickListener customOnClickListener) {
        this.bookmarkList = getDataForListView();
        this.mContext = mContext;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mRecyclerView = mRecyclerView;
        this.customOnClickListener = customOnClickListener;
        this.customOnLongClickListener = null;
    }

    public BookmarkListAdapter(Context mContext, View.OnLongClickListener customOnLongClickListener, View.OnClickListener customOnClickListener, RecyclerView mRecyclerView) {
        this.customOnLongClickListener = customOnLongClickListener;
        this.customOnClickListener = customOnClickListener;
        this.mRecyclerView = mRecyclerView;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        this.mContext = mContext;
        this.bookmarkList = getDataForListView();
    }

    private List<Bookmark> getDataForListView() {

        List<Bookmark> bookmarks = new ArrayList<Bookmark>();

        bookmarks.add( new Bookmark("Important", String.format("#%06X",0xFFFFFF & Color.RED))); // Bookmark Important
        bookmarks.add( new Bookmark("Didn't Understand", String.format("#%06X",0xFFFFFF & Color.BLUE))); // Bookmark Don't UnderStand
        bookmarks.add( new Bookmark("Can't Hear", String.format("#%06X",0xFFFFFF & Color.YELLOW))); // Bookmark Can't Hear
        bookmarks.add( new Bookmark("Bookmark 4", String.format("#%06X",0xFFFFFF & Color.GRAY))); // Bookmark Can't Hear
        bookmarks.add( new Bookmark("Bookmark 5", String.format("#%06X",0xFFFFFF & Color.BLACK))); // Bookmark Can't Hear
        bookmarks.add(new Bookmark("Bookmark 6", String.format("#%06X", 0xFFFFFF & Color.GREEN))); // Bookmark Can't Hear

        return bookmarks;

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

        // Inflate view and Attach Click Listeners
        View view = mLayoutInflater.inflate(R.layout.bookmark_list_item, parent, false);

        if ( customOnClickListener != null ) {
            view.setOnClickListener(customOnClickListener);
        }

        // Guessing this might be deleting for editing bookmarks
        if ( customOnLongClickListener != null ) {
            view.setOnLongClickListener(customOnLongClickListener);
        }

//        view.setOnLongClickListener(new RecyclerOnLongClickListener());

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Bookmark bookmark = bookmarkList.get(position);
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
        return bookmarkList.size();
    }

    public class BookmarkOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            // TODO
            // Leave Bookmark
            Bookmark bookmark = bookmarkList.get(findPosition(v));
            // Do Something with Bookmark

            showToast( bookmarkList.get(findPosition(v)).toString() );
        }
    }

    private int findPosition ( View v ) {
        return mRecyclerView.getChildLayoutPosition(v);
    }

    public void showToast( String msg ) {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
    }

    public Bookmark findBookmark ( View v ) {
        return bookmarkList.get( findPosition(v) );
    }

    public void setCustomOnClickListener(View.OnClickListener customOnClickListener) {
        this.customOnClickListener = customOnClickListener;
    }
}
