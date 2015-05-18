package com.loroclip;

import android.app.Dialog;
import android.content.Context;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;

import com.loroclip.model.BookmarkHistory;

import java.util.ArrayList;

/**
 * Created by minhyeok on 5/13/15.
 */
public class SavedBookmarkHistoryListDialog extends Dialog {
    private ArrayList<BookmarkHistory> bookmarkHistoryList;
    private BookmarkListView bookmarkHistoryListView;
    private Message response;

    public SavedBookmarkHistoryListDialog(Context context, String fileName, Message msg) {
        super(context);

        setContentView(R.layout.saved_bookmark_history_list_dialog);

        setTitle("북마크 선택");

        bookmarkHistoryListView = (BookmarkListView) findViewById(R.id.bookmark_dialog_listview);
        SavedBookmarkHistoryListAdapter adapter = new SavedBookmarkHistoryListAdapter(fileName);
        bookmarkHistoryListView.setAdapter(adapter);
        bookmarkHistoryList = (ArrayList<BookmarkHistory>) adapter.getSavedBookmarkHistory();

        response = msg;

        bookmarkHistoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                response.arg1 = bookmarkHistoryList.get(i).getStartMiiliseconds();
                response.sendToTarget();
                dismiss();
            }
        });
    }
}
