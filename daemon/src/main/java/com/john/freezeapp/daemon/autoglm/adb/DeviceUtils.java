package com.john.freezeapp.daemon.autoglm.adb;

import android.os.Build;

import com.john.freezeapp.daemon.autoglm.config.Apps;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

/**
 * Device control utilities for Android automation.
 */
public class DeviceUtils {

    /**
     * Get the currently focused app name.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static String getCurrentApp(String deviceId) throws Exception {
        // Use dumpsys window directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{"dumpsys", "window"});
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            process.waitFor(5, TimeUnit.SECONDS);
        } else {
            process.waitFor();
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("mCurrentFocus") || line.contains("mFocusedApp")) {
                    for (String appName : Apps.getAppPackages().keySet()) {
                        String packageName = Apps.getAppPackages().get(appName);
                        if (line.contains(packageName)) {
                            return appName;
                        }
                    }
                }
            }
        }
        
        return "System Home";
    }

    /**
     * Tap at the specified coordinates.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void tap(int x, int y, String deviceId) throws Exception {
        tap(x, y, deviceId, 1.0);
    }

    public static void tap(int x, int y, String deviceId, double delay) throws Exception {
        // Use Android shell input command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "input", "tap", String.valueOf(x), String.valueOf(y)
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
    }

    /**
     * Double tap at the specified coordinates.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void doubleTap(int x, int y, String deviceId) throws Exception {
        doubleTap(x, y, deviceId, 1.0);
    }

    public static void doubleTap(int x, int y, String deviceId, double delay) throws Exception {
        tap(x, y, deviceId, 0.1);
        tap(x, y, deviceId, delay);
    }

    /**
     * Long press at the specified coordinates.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void longPress(int x, int y, String deviceId) throws Exception {
        longPress(x, y, 3000, deviceId, 1.0);
    }

    public static void longPress(int x, int y, int durationMs, String deviceId, double delay) 
            throws Exception {
        // Use Android shell input command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "input", "swipe",
            String.valueOf(x), String.valueOf(y),
            String.valueOf(x), String.valueOf(y),
            String.valueOf(durationMs)
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
    }

    /**
     * Swipe from start to end coordinates.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void swipe(int startX, int startY, int endX, int endY, String deviceId) 
            throws Exception {
        swipe(startX, startY, endX, endY, null, deviceId, 1.0);
    }

    public static void swipe(int startX, int startY, int endX, int endY, 
            Integer durationMs, String deviceId, double delay) throws Exception {
        if (durationMs == null) {
            // Calculate duration based on distance
            double distSq = Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2);
            durationMs = (int)(distSq / 1000);
            durationMs = Math.max(1000, Math.min(durationMs, 2000)); // Clamp between 1000-2000ms
        }

        // Use Android shell input command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "input", "swipe",
            String.valueOf(startX), String.valueOf(startY),
            String.valueOf(endX), String.valueOf(endY),
            String.valueOf(durationMs)
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
    }

    /**
     * Press the back button.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void back(String deviceId) throws Exception {
        back(deviceId, 1.0);
    }

    public static void back(String deviceId, double delay) throws Exception {
        // Use Android shell input command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "input", "keyevent", "4"
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
    }

    /**
     * Press the home button.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void home(String deviceId) throws Exception {
        home(deviceId, 1.0);
    }

    public static void home(String deviceId, double delay) throws Exception {
        // Use Android shell input command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "input", "keyevent", "KEYCODE_HOME"
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
    }

    /**
     * Launch an app by name.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static boolean launchApp(String appName, String deviceId) throws Exception {
        return launchApp(appName, deviceId, 1.0);
    }

    public static boolean launchApp(String appName, String deviceId, double delay) 
            throws Exception {
        String packageName = Apps.getPackageName(appName);
        if (packageName == null) {
            return false;
        }

        // Use monkey command directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "monkey",
            "-p", packageName,
            "-c", "android.intent.category.LAUNCHER",
            "1"
        });
        process.waitFor();
        Thread.sleep((long)(delay * 1000));
        return true;
    }
}

