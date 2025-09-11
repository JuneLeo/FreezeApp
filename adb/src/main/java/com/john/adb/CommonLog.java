package com.john.adb;

import android.util.Log;

public class CommonLog {
    public static void log(String s) {
        Log.d("FreezeAdb", s);
    }

    public static void error(String s) {
        Log.e("FreezeAdb", s);
    }
}
