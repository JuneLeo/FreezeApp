// IAutoGLMBinder.aidl
package com.john.freezeapp.daemon.autoglm;
import com.john.freezeapp.daemon.autoglm.IAutoGLMListener;

// Declare any non-default types here with import statements

interface IAutoGLMBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void execute(String query, String url, String model, String apiKey);

    boolean isActive();

    void stop();

    void addListener(IAutoGLMListener listener);

    void removeListener(IAutoGLMListener listener);
}