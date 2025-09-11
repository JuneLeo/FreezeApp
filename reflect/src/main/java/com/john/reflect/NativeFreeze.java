package com.john.reflect;

public class NativeFreeze {
    static {
        System.loadLibrary("freeze");
    }

    public static native void nativeInit(int targetSdkVersion);
}
