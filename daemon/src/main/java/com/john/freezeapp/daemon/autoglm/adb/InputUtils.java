package com.john.freezeapp.daemon.autoglm.adb;

import android.os.Build;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Input utilities for Android device text input.
 */
public class InputUtils {

    /**
     * Type text into the currently focused input field using ADB Keyboard.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void typeText(String text, String deviceId) throws Exception {
        String encodedText = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            encodedText = Base64.getEncoder().encodeToString(
                text.getBytes(StandardCharsets.UTF_8));
        }

        // Use am broadcast directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "am", "broadcast",
            "-a", "ADB_INPUT_B64",
            "--es", "msg", encodedText
        });
        process.waitFor();
    }

    /**
     * Clear text in the currently focused input field.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void clearText(String deviceId) throws Exception {
        // Use am broadcast directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "am", "broadcast", "-a", "ADB_CLEAR_TEXT"
        });
        process.waitFor();
    }

    /**
     * Detect current keyboard and switch to ADB Keyboard if needed.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static String detectAndSetAdbKeyboard(String deviceId) throws Exception {
        // Use settings get directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "settings", "get", "secure", "default_input_method"
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            process.waitFor(5, TimeUnit.SECONDS);
        } else {
            process.waitFor();
        }

        String currentIme = "";
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()))) {
            currentIme = reader.readLine();
            if (currentIme == null) {
                currentIme = "";
            }
        }
        
        // Switch to ADB Keyboard if not already set
        if (!currentIme.contains("com.android.adbkeyboard/.AdbIME")) {
            // Use ime set directly (no ADB needed)
            Process setProcess = Runtime.getRuntime().exec(new String[]{
                "ime", "set", "com.android.adbkeyboard/.AdbIME"
            });
            setProcess.waitFor();
        }

        // Warm up the keyboard
        typeText("", deviceId);
        
        return currentIme.trim();
    }

    /**
     * Restore the original keyboard IME.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static void restoreKeyboard(String ime, String deviceId) throws Exception {
        // Use ime set directly (no ADB needed)
        Process process = Runtime.getRuntime().exec(new String[]{
            "ime", "set", ime
        });
        process.waitFor();
    }
}

