package com.john.adb;

public class AdbProtocol {
    public static final int A_SYNC = 0x434e5953;
    public static final int A_CNXN = 0x4e584e43;
    public static final int A_AUTH = 0x48545541;
    public static final int A_OPEN = 0x4e45504f;
    public static final int A_OKAY = 0x59414b4f;
    public static final int A_CLSE = 0x45534c43;
    public static final int A_WRTE = 0x45545257;
    public static final int A_STLS = 0x534C5453;

    public static final int A_VERSION = 0x01000000;
    public static final int A_MAXDATA = 4096;

    public static final int A_STLS_VERSION = 0x01000000;

    public static final int ADB_AUTH_TOKEN = 1;
    public static final int ADB_AUTH_SIGNATURE = 2;
    public static final int ADB_AUTH_RSAPUBLICKEY = 3;
}
