package com.john.freezeapp.daemon;

public class DaemonHelper {
    public static final String FREEZE_DAEMON_PREFIX = "FreezeApp_";
    public static final String DAEMON_NICKNAME = FREEZE_DAEMON_PREFIX + "com.john.freezeapp";
    public static final String DAEMON_RUNAS_PREFIX =  "FreezeApp_RunAs_";
    public static final String DAEMON_RUNAS_FILTER_PREFIX =  "[F]reezeApp_RunAs_";

    public static final String DAEMON_MODULE_CUSTOM = "FreezeApp";
    public static final String DAEMON_MODULE_SYSTEM = "System";
    public static final String DAEMON_MODULE_SYSTEM_PROPERTIES = "SystemProperties";
    public static final String KEY_DAEMON_VERSION = "key_daemon_version";
    public static final String KEY_DAEMON_PACKAGE_NAME = "key_daemon_package_name";
    public static final String KEY_DAEMON_PID = "key_daemon_pid";
    public static final String KEY_DAEMON_UID = "key_daemon_uid";
    public static final String KEY_DAEMON_USERID = "key_daemon_userid";

    public static final String DAEMON_SHELL_PACKAGE = "com.android.shell";
    public static final String DAEMON_BINDER_FRP = "daemon_binder_frp";
    public static final String DAEMON_BINDER_APP_MONITOR = "daemon_binder_app_monitor";
    public static final String DAEMON_BINDER_CLIPBOARD_MONITOR = "daemon_binder_clipboard_monitor";
    public static final String DAEMON_BINDER_FILE_SERVER = "daemon_binder_file_server";
    public static final String DAEMON_BINDER_RUN_AS = "daemon_binder_run_as";
    public static final String DAEMON_BINDER_TRAFFIC_MONITOR = "daemon_binder_traffic_monitor";

    public static final String DAEMON_BINDER_AUTO_GLM = "daemon_binder_auto_glm";


    public static final String DAEMON_CLIPBOARD_PATH = "/sdcard/Android/.freezeapp/clipboard_data.xml";
    public static final String DAEMON_CONFIG_PATH = "/sdcard/Android/.freezeapp/config.xml";
    public static final String SP_KEY_CLIPBOARD_SWITCHER = "sp_key_clipboard_switcher";
}
