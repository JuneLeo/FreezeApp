package com.john.freezeapp.daemon.autoglm.actions;

/**
 * Result of an action execution.
 */
public class ActionResult {
    private boolean success;
    private boolean shouldFinish;
    private String message;
    private boolean requiresConfirmation;

    public ActionResult(boolean success, boolean shouldFinish) {
        this(success, shouldFinish, null, false);
    }

    public ActionResult(boolean success, boolean shouldFinish, String message) {
        this(success, shouldFinish, message, false);
    }

    public ActionResult(boolean success, boolean shouldFinish, String message, 
            boolean requiresConfirmation) {
        this.success = success;
        this.shouldFinish = shouldFinish;
        this.message = message;
        this.requiresConfirmation = requiresConfirmation;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isShouldFinish() {
        return shouldFinish;
    }

    public void setShouldFinish(boolean shouldFinish) {
        this.shouldFinish = shouldFinish;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRequiresConfirmation() {
        return requiresConfirmation;
    }

    public void setRequiresConfirmation(boolean requiresConfirmation) {
        this.requiresConfirmation = requiresConfirmation;
    }
}

