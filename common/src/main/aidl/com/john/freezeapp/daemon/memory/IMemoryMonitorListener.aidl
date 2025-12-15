// IAutoGLMBinder.aidl
package com.john.freezeapp.daemon.memory;
import com.john.freezeapp.daemon.memory.MemoryData;

interface IMemoryMonitorListener {
    void process(in MemoryData data);
}