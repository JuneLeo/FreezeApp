package com.john.freezeapp.daemon.autoglm;

import com.google.gson.JsonObject;

/**
 * Result of a single agent step.
 */
public class StepResult {
    private boolean success;
    private boolean finished;
    private JsonObject action;
    private String thinking;
    private String message;

    public StepResult(boolean success, boolean finished, JsonObject action, 
            String thinking, String message) {
        this.success = success;
        this.finished = finished;
        this.action = action;
        this.thinking = thinking;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public JsonObject getAction() {
        return action;
    }

    public void setAction(JsonObject action) {
        this.action = action;
    }

    public String getThinking() {
        return thinking;
    }

    public void setThinking(String thinking) {
        this.thinking = thinking;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

