package com.john.adb;

public class PairingContext {


    private long nativePtr;
    private byte[] msg;

    static {
        System.loadLibrary("adb");
    }

    public PairingContext(long nativePtr) {
        this.nativePtr = nativePtr;
        msg = nativeMsg(nativePtr);

    }

    public boolean initCipher(byte[] theirMsg) {
        return nativeInitCipher(nativePtr, theirMsg);
    }

    public byte[] encrypt(byte[] in) {
        return nativeEncrypt(nativePtr, in);
    }

    public byte[] decrypt(byte[] in) {
        return nativeDecrypt(nativePtr, in);
    }

    public void destroy() {
        nativeDestroy(nativePtr);
    }


    private native byte[] nativeMsg(long nativePtr);

    private native boolean nativeInitCipher(long nativePtr, byte[] theirMsg);

    private native byte[] nativeEncrypt(long nativePtr, byte[] inbuf);

    private native byte[] nativeDecrypt(long nativePtr, byte[] inbuf);

    private native void nativeDestroy(long nativePtr);

    public static PairingContext create(byte[] password) {
        long nativePtr = nativeConstructor(true, password);
        return nativePtr != 0L ? new PairingContext(nativePtr) : null;
    }


    private static native long nativeConstructor(boolean isClient, byte[] password);

    public byte[] getMsg() {
        return msg;
    }
}