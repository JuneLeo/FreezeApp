// IAutoGLMBinder.aidl
package com.john.freezeapp.daemon.autoglm;

interface IAutoGLMListener {
    void start();
    void end();
    void process(String action);

}