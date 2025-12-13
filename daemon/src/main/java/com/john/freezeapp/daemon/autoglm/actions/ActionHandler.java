package com.john.freezeapp.daemon.autoglm.actions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.daemon.autoglm.adb.DeviceUtils;
import com.john.freezeapp.daemon.autoglm.adb.InputUtils;

import java.util.function.Function;

/**
 * Handles execution of actions from AI model output.
 */
public class ActionHandler {
    private String deviceId;
    private Function<String, Boolean> confirmationCallback;
    private Function<String, Void> takeoverCallback;

    public ActionHandler(String deviceId) {
        this(deviceId, null, null);
    }

    public ActionHandler(String deviceId, Function<String, Boolean> confirmationCallback,
            Function<String, Void> takeoverCallback) {
        this.deviceId = deviceId;
        this.confirmationCallback = confirmationCallback != null ? 
            confirmationCallback : this::defaultConfirmation;
        this.takeoverCallback = takeoverCallback != null ? 
            takeoverCallback : this::defaultTakeover;
    }

    /**
     * Execute an action from the AI model.
     */
    public ActionResult execute(JsonObject action, int screenWidth, int screenHeight) {
        String actionType = action.has("_metadata") ? 
            action.get("_metadata").getAsString() : null;

        if ("finish".equals(actionType)) {
            String message = action.has("message") ? 
                action.get("message").getAsString() : null;
            return new ActionResult(true, true, message);
        }

        if (!"do".equals(actionType)) {
            return new ActionResult(false, true, 
                "Unknown action type: " + actionType);
        }

        String actionName = action.has("action") ? 
            action.get("action").getAsString() : null;

        try {
            switch (actionName) {
                case "Launch":
                    return handleLaunch(action, screenWidth, screenHeight);
                case "Tap":
                    return handleTap(action, screenWidth, screenHeight);
                case "Type":
                case "Type_Name":
                    return handleType(action, screenWidth, screenHeight);
                case "Swipe":
                    return handleSwipe(action, screenWidth, screenHeight);
                case "Back":
                    return handleBack(action, screenWidth, screenHeight);
                case "Home":
                    return handleHome(action, screenWidth, screenHeight);
                case "Double Tap":
                    return handleDoubleTap(action, screenWidth, screenHeight);
                case "Long Press":
                    return handleLongPress(action, screenWidth, screenHeight);
                case "Wait":
                    return handleWait(action, screenWidth, screenHeight);
                case "Take_over":
                    return handleTakeover(action, screenWidth, screenHeight);
                default:
                    return new ActionResult(false, false, 
                        "Unknown action: " + actionName);
            }
        } catch (Exception e) {
            return new ActionResult(false, false, "Action failed: " + e.getMessage());
        }
    }

    private int[] convertRelativeToAbsolute(JsonArray element, 
            int screenWidth, int screenHeight) {
        int x = (int)(element.get(0).getAsInt() / 1000.0 * screenWidth);
        int y = (int)(element.get(1).getAsInt() / 1000.0 * screenHeight);
        return new int[]{x, y};
    }

    private ActionResult handleLaunch(JsonObject action, int width, int height) {
        String appName = action.has("app") ? action.get("app").getAsString() : null;
        if (appName == null || appName.isEmpty()) {
            return new ActionResult(false, false, "No app name specified");
        }

        try {
            boolean success = DeviceUtils.launchApp(appName, deviceId);
            return success ? new ActionResult(true, false) : 
                new ActionResult(false, false, "App not found: " + appName);
        } catch (Exception e) {
            return new ActionResult(false, false, "Launch failed: " + e.getMessage());
        }
    }

    private ActionResult handleTap(JsonObject action, int width, int height) {
        if (!action.has("element")) {
            return new ActionResult(false, false, "No element coordinates");
        }

        JsonArray element = action.get("element").getAsJsonArray();
        int[] coords = convertRelativeToAbsolute(element, width, height);

        // Check for sensitive operation
        if (action.has("message")) {
            String message = action.get("message").getAsString();
            if (!confirmationCallback.apply(message)) {
                return new ActionResult(false, true, "User cancelled sensitive operation");
            }
        }

        try {
            DeviceUtils.tap(coords[0], coords[1], deviceId);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Tap failed: " + e.getMessage());
        }
    }

    private ActionResult handleType(JsonObject action, int width, int height) {
        String text = action.has("text") ? action.get("text").getAsString() : "";

        try {
            // Switch to ADB keyboard
            String originalIme = InputUtils.detectAndSetAdbKeyboard(deviceId);
            Thread.sleep(1000);

            // Clear existing text and type new text
            InputUtils.clearText(deviceId);
            Thread.sleep(1000);

            InputUtils.typeText(text, deviceId);
            Thread.sleep(1000);

            // Restore original keyboard
            InputUtils.restoreKeyboard(originalIme, deviceId);
            Thread.sleep(1000);

            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Type failed: " + e.getMessage());
        }
    }

    private ActionResult handleSwipe(JsonObject action, int width, int height) {
        if (!action.has("start") || !action.has("end")) {
            return new ActionResult(false, false, "Missing swipe coordinates");
        }

        JsonArray start = action.get("start").getAsJsonArray();
        JsonArray end = action.get("end").getAsJsonArray();
        int[] startCoords = convertRelativeToAbsolute(start, width, height);
        int[] endCoords = convertRelativeToAbsolute(end, width, height);

        try {
            DeviceUtils.swipe(startCoords[0], startCoords[1], 
                endCoords[0], endCoords[1], null, deviceId, 1.0);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Swipe failed: " + e.getMessage());
        }
    }

    private ActionResult handleBack(JsonObject action, int width, int height) {
        try {
            DeviceUtils.back(deviceId);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Back failed: " + e.getMessage());
        }
    }

    private ActionResult handleHome(JsonObject action, int width, int height) {
        try {
            DeviceUtils.home(deviceId);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Home failed: " + e.getMessage());
        }
    }

    private ActionResult handleDoubleTap(JsonObject action, int width, int height) {
        if (!action.has("element")) {
            return new ActionResult(false, false, "No element coordinates");
        }

        JsonArray element = action.get("element").getAsJsonArray();
        int[] coords = convertRelativeToAbsolute(element, width, height);

        try {
            DeviceUtils.doubleTap(coords[0], coords[1], deviceId);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Double tap failed: " + e.getMessage());
        }
    }

    private ActionResult handleLongPress(JsonObject action, int width, int height) {
        if (!action.has("element")) {
            return new ActionResult(false, false, "No element coordinates");
        }

        JsonArray element = action.get("element").getAsJsonArray();
        int[] coords = convertRelativeToAbsolute(element, width, height);

        try {
            DeviceUtils.longPress(coords[0], coords[1], deviceId);
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Long press failed: " + e.getMessage());
        }
    }

    private ActionResult handleWait(JsonObject action, int width, int height) {
        String durationStr = action.has("duration") ? 
            action.get("duration").getAsString() : "1 seconds";
        
        try {
            double duration = Double.parseDouble(
                durationStr.replace("seconds", "").trim());
            Thread.sleep((long)(duration * 1000));
            return new ActionResult(true, false);
        } catch (Exception e) {
            return new ActionResult(false, false, "Wait failed: " + e.getMessage());
        }
    }

    private ActionResult handleTakeover(JsonObject action, int width, int height) {
        String message = action.has("message") ? 
            action.get("message").getAsString() : "User intervention required";
        takeoverCallback.apply(message);
        return new ActionResult(true, false);
    }

    private Boolean defaultConfirmation(String message) {
        System.out.print("Sensitive operation: " + message + "\nConfirm? (Y/N): ");
        try {
            java.util.Scanner scanner = new java.util.Scanner(System.in);
            String response = scanner.nextLine();
            return "Y".equalsIgnoreCase(response);
        } catch (Exception e) {
            return false;
        }
    }

    private Void defaultTakeover(String message) {
        DaemonLog.log(message);
        System.out.print("Press Enter after completing manual operation...");
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}

