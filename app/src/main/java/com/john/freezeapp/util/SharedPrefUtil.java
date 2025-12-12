package com.john.freezeapp.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.john.freezeapp.App;

public class SharedPrefUtil {

    private static final String NAMESPACE = "FreezeApp";
    public static final String KEY_KERNEL_VERSION = "KEY_KERNEL_VERSION";
    public static final String KEY_FIRST_BIND_DAEMON = "KEY_FIRST_BIND_DAEMON";
    public static final String KEY_FIRST_UNBIND_DAEMON = "KEY_FIRST_UNBIND_DAEMON";
    public static final String KEY_APP_MONITOR_SWITCHER = "key_app_monitor_switcher";
    public static final String KEY_APP_MONITOR_TEXT_SIZE = "key_app_monitor_text_size";
    public static final String KEY_SETTING_NIGHT_MODE = "key_setting_night_mode";
    public static final String KEY_CLIPBOARD_FLOAT_SWITCHER = "key_clipboard_float_switcher";
    public static final String KEY_DAEMON_SHELL_VERSION = "key_daemon_shell_version";
    public static final String KEY_BUILD_TIMESTAMP = "key_build_timestamp";
    public static final String KEY_DAEMON_APK_VERSION = "key_daemon_apk_version";
    public static final String KEY_TOOL_STYLE = "key_tool_style";
    public static final String KEY_TRAFFIC_THRESHOLD = "key_traffic_threshold";
    public static final String KEY_TRAFFIC_SWITCHER = "key_traffic_switcher";
    public static final String KEY_TRAFFIC_TYPE = "key_traffic_match_rule";
    public static final String KEY_MEMORY_MONITOR_SWITCH = "key_memory_monitor_switch";
    public static final String KEY_MEMORY_MONITOR_PROCESS_NAME = "key_memory_monitor_process_name";

    public static SharedPreferences getSharedPref() {
        return App.getApp().getSharedPreferences(NAMESPACE, Context.MODE_PRIVATE);
    }

    public static String getString(String key, String def) {
        return getSharedPref().getString(key, def);
    }

    public static void setString(String key, String def) {
        SharedPreferences.Editor edit = getSharedPref().edit();
        edit.putString(key, def);
        edit.apply();
    }

    public static boolean getBoolean(String key, boolean def) {
        return getSharedPref().getBoolean(key, def);
    }

    public static void setBoolean(String key, boolean value) {
        SharedPreferences.Editor edit = getSharedPref().edit();
        edit.putBoolean(key, value);
        edit.apply();
    }


    public static int getInt(String key, int def) {
        return getSharedPref().getInt(key, def);
    }

    public static void setInt(String key, int value) {
        SharedPreferences.Editor edit = getSharedPref().edit();
        edit.putInt(key, value);
        edit.apply();
    }


    public static long getLong(String key, long def) {
        return getSharedPref().getLong(key, def);
    }

    public static void setLong(String key, long value) {
        SharedPreferences.Editor edit = getSharedPref().edit();
        edit.putLong(key, value);
        edit.apply();
    }


    public static boolean isFirstBindDaemon() {
        return getBoolean(KEY_FIRST_BIND_DAEMON, true);
    }

    public static void setFirstBindDaemon() {
        setBoolean(KEY_FIRST_BIND_DAEMON, false);
    }


    public static boolean isFirstUnbindDaemon() {
        return getBoolean(KEY_FIRST_UNBIND_DAEMON, true);
    }

    public static void setFirstUnbindDaemon() {
        setBoolean(KEY_FIRST_UNBIND_DAEMON, false);
    }
}
