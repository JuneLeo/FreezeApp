// IAutoGLMBinder.aidl
package com.john.freezeapp.daemon.memory;
import com.john.freezeapp.daemon.memory.IMemoryMonitorListener;

// Declare any non-default types here with import statements

interface IMemoryMonitorBinder {

    boolean start(String packageName, long delay);

    boolean isActive();

    void stop();

    void addListener(IMemoryMonitorListener listener);

    void removeListener(IMemoryMonitorListener listener);
}