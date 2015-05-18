package com.loroclip;

import com.loroclip.model.Bookmark;

import java.util.HashMap;
import java.util.List;

/**
 * Created by minhyeok on 5/8/15.
 */
public class BookmarkMap extends HashMap<String, Integer> {
    public BookmarkMap() {
        List<Bookmark> bmList = new Bookmark().initializeBookmark();
        for (Bookmark bookmark : bmList){
            put(bookmark.getName(), bookmark.getColor());
        }
    }

}
