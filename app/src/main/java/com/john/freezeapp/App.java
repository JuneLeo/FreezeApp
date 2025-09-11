package com.john.freezeapp;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.john.freezeapp.clipboard.Clipboard;
import com.john.freezeapp.monitor.AppMonitorManager;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.SettingUtil;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class App extends Application {

    public static App sApp;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sApp = this;
        AppCompatDelegate.setDefaultNightMode(SettingUtil.getNightMode());
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DeviceUtil.atLeast28()) {
            HiddenApiBypass.addHiddenApiExemptions("L");
        }
        AppMonitorManager.startAppMonitor(getApp());
        Clipboard.startClipboardFloating(getApp());
//        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
//            Log.d("FreezeApp", e.getMessage());
//            Log.d("FreezeApp", e.getMessage());
//        });
    }

    public static App getApp() {
        return sApp;
    }
}
