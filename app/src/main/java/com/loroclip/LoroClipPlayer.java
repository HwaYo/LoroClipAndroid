package com.loroclip;

import android.media.MediaPlayer;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by minhyeok on 5/18/15.
 */
public class LoroClipPlayer extends MediaPlayer {
    public LoroClipPlayer(String filepath) {
        try {
            FileInputStream fis = new FileInputStream(filepath);
            FileDescriptor fd = fis.getFD();
            setDataSource(fd);
            prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start(int milliSec) {
        try {
            stop();
            prepare();
            seekTo(milliSec);
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
