package com.loroclip.util;

import android.graphics.Color;

import org.json.JSONArray;

/**
 * Created by minhyeok on 5/13/15.
 */
public class Util {
    public static String secondsToMinutesStr(int integerSecs){
        // Turn, e.g. 67 seconds into "1:07"
        String timecodeMinutes = "" + (integerSecs / 60);
        String timecodeSeconds = "" + (integerSecs % 60);
        if ((integerSecs % 60) < 10) {
            timecodeSeconds = "0" + timecodeSeconds;
        }
        String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;

        return timecodeStr;
    }

    public static String milliSecondsToMinutesStr(int integerMilliSecs){
        return secondsToMinutesStr(integerMilliSecs / 1000);
    }

    public static int[] JSONArrayToIntArray(JSONArray array){
        if (array == null) { /*...*/ }

        int[] numbers = new int[array.length()];

        for (int i = 0; i < array.length(); ++i) {
            numbers[i] = array.optInt(i);
        }

        return numbers;
    }

    public static int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }
}
