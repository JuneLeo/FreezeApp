package com.john.freezeapp.daemon.autoglm.adb;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Build;

import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.util.ImageUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Screenshot utilities for capturing Android device screen.
 */
public class ScreenshotUtils {
    private static final int DEFAULT_WIDTH = 1080;
    private static final int DEFAULT_HEIGHT = 2400;
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Capture a screenshot from the connected Android device.
     * Note: deviceId parameter is kept for compatibility but not used in Android native mode.
     */
    public static Screenshot getScreenshot(String deviceId) throws IOException {
        try {
            // Use Android shell command to capture screenshot directly to stream
            // Since we're running on Android device, use screencap directly
            DaemonLog.log("getScreenshot exec start");
            Process process = Runtime.getRuntime().exec(new String[]{"screencap", "-p", "/sdcard/tmp.png"});
            DaemonLog.log("getScreenshot exec end");
            // Wait for process with timeout
            boolean finished = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            } else {
                finished = process.waitFor() == 0;
            }


            DaemonLog.log("getScreenshot wait end");

            if (!finished) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    process.destroyForcibly();
                } else {
                    process.destroy();
                }
                return createFallbackScreenshot(false);
            }

            // Check exit code
            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // Check for sensitive screen error
                InputStream errorStream = process.getErrorStream();
                if (errorStream != null) {
                    try {
                        byte[] errorBytes = new byte[errorStream.available()];
                        if (errorBytes.length > 0) {
                            errorStream.read(errorBytes);
                            String errorMsg = new String(errorBytes);
                            if (errorMsg.contains("Status: -1") || errorMsg.contains("Failed")) {
                                return createFallbackScreenshot(true);
                            }
                        }
                    } catch (Exception e) {
                        // Ignore error reading error stream
                    }
                }
                return createFallbackScreenshot(false);
            }

//            // Read screenshot data from input stream
//            InputStream inputStream = process.getInputStream();
//            if (inputStream == null) {
//                return createFallbackScreenshot(false);
//            }
//
//
//
            FileInputStream inputStream = new FileInputStream("/sdcard/tmp.png");
            // Read all bytes from input stream
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] imageBytes = buffer.toByteArray();

            if (imageBytes.length == 0) {
                return createFallbackScreenshot(false);
            }

            // Decode bitmap from bytes using Android API
            Bitmap originalBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            if (originalBitmap == null) {
                DaemonLog.log("originalBitmap = null");
                return createFallbackScreenshot(false);
            }

            int originalWidth = originalBitmap.getWidth();
            int originalHeight = originalBitmap.getHeight();
            
            // Aggressively compress image to reduce token usage
            // Use much smaller resolution (max 384px width) to significantly reduce tokens
            // Vision models can work with lower resolution images
            int maxWidth = 384;  // Further reduced to 384px for minimal token usage
            int maxHeight = 768; // Limit height as well
            
//            int targetWidth = originalWidth;
//            int targetHeight = originalHeight;
//
//            // Resize bitmap if needed (currently keeping original size, but can be enabled)
//            Bitmap compressedBitmap = originalBitmap;
//            if (originalWidth > maxWidth || originalHeight > maxHeight) {
//                // Calculate target dimensions maintaining aspect ratio
//                float scaleWidth = (float) maxWidth / originalWidth;
//                float scaleHeight = (float) maxHeight / originalHeight;
//                float scale = Math.min(scaleWidth, scaleHeight);
//
//                targetWidth = (int) (originalWidth * scale);
//                targetHeight = (int) (originalHeight * scale);
//
//                compressedBitmap = Bitmap.createScaledBitmap(
//                    originalBitmap, targetWidth, targetHeight, true);
//
//                // Recycle original if we created a new one
//                if (compressedBitmap != originalBitmap) {
//                    originalBitmap.recycle();
//                }
//            }
            
            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            // Use JPEG compression with lower quality to reduce size further
            byte[] compressedBytes = ImageUtils.bitmapToByteArray(
                    originalBitmap, Bitmap.CompressFormat.JPEG, 40); // 40% quality - very aggressive compression

            if (compressedBytes == null) {
                // Fallback to PNG if JPEG compression fails
                compressedBytes = ImageUtils.bitmapToByteArray(
                        originalBitmap, Bitmap.CompressFormat.PNG, 100);
            }

            if (compressedBytes == null) {
                originalBitmap.recycle();
                return createFallbackScreenshot(false);
            }

            String base64Data = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                base64Data = Base64.getEncoder().encodeToString(compressedBytes);
            }

            // Debug: print image size info
            int base64Size = base64Data.length();
            // Rough estimate: base64 is ~4/3 the size of binary, and tokens are roughly base64_size / 4
            // For vision models, images are typically tokenized more efficiently, but this gives a rough estimate
            long estimatedTokens = base64Size / 4;
            DaemonLog.log("DEBUG: Image compressed to " + width + "x" + height +
                ", Base64 size: " + (base64Size / 1024) + " KB, Estimated tokens: ~" + estimatedTokens);

            // Cleanup
            originalBitmap.recycle();

            return new Screenshot(base64Data, width, height, false);

        } catch (Exception e) {
            System.err.println("Screenshot error: " + e.getMessage());
            e.printStackTrace();
            return createFallbackScreenshot(false);
        }
    }

    /**
     * Create a black fallback image when screenshot fails.
     */
    private static Screenshot createFallbackScreenshot(boolean isSensitive) {
        try {
            // Create black bitmap using Android API
            Bitmap blackBitmap = Bitmap.createBitmap(
                DEFAULT_WIDTH, DEFAULT_HEIGHT, Bitmap.Config.RGB_565);
            
            // Fill with black using Canvas
            Canvas canvas = new Canvas(blackBitmap);
            canvas.drawColor(0xFF000000); // Black color
            
            // Convert to byte array using ImageUtils
            byte[] imageBytes = ImageUtils.bitmapToByteArray(
                blackBitmap, Bitmap.CompressFormat.PNG, 100);
            
            if (imageBytes == null) {
                blackBitmap.recycle();
                return new Screenshot("", DEFAULT_WIDTH, DEFAULT_HEIGHT, isSensitive);
            }

            String base64Data = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                base64Data = Base64.getEncoder().encodeToString(imageBytes);
            }

            // Cleanup
            blackBitmap.recycle();

            return new Screenshot(base64Data, DEFAULT_WIDTH, DEFAULT_HEIGHT, isSensitive);
        } catch (Exception e) {
            System.err.println("Fallback screenshot error: " + e.getMessage());
            // Fallback to empty base64
            return new Screenshot("", DEFAULT_WIDTH, DEFAULT_HEIGHT, isSensitive);
        }
    }
}

