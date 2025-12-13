package com.john.freezeapp.daemon.autoglm.adb;

/**
 * Represents a captured screenshot.
 */
public class Screenshot {
    private String base64Data;
    private int width;
    private int height;
    private boolean isSensitive;

    public Screenshot(String base64Data, int width, int height, boolean isSensitive) {
        this.base64Data = base64Data;
        this.width = width;
        this.height = height;
        this.isSensitive = isSensitive;
    }

    public Screenshot(String base64Data, int width, int height) {
        this(base64Data, width, height, false);
    }

    public String getBase64Data() {
        return base64Data;
    }

    public void setBase64Data(String base64Data) {
        this.base64Data = base64Data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isSensitive() {
        return isSensitive;
    }

    public void setSensitive(boolean sensitive) {
        isSensitive = sensitive;
    }
}

