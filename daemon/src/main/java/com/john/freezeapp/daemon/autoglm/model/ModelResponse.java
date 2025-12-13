package com.john.freezeapp.daemon.autoglm.model;

/**
 * Response from the AI model.
 */
public class ModelResponse {
    private String thinking;
    private String action;
    private String rawContent;

    public ModelResponse(String thinking, String action, String rawContent) {
        this.thinking = thinking;
        this.action = action;
        this.rawContent = rawContent;
    }

    public String getThinking() {
        return thinking;
    }

    public void setThinking(String thinking) {
        this.thinking = thinking;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getRawContent() {
        return rawContent;
    }

    public void setRawContent(String rawContent) {
        this.rawContent = rawContent;
    }
}

