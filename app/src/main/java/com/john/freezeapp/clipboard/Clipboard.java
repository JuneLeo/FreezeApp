package com.john.freezeapp.clipboard;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.daemon.clipboard.ClipboardData;
import com.john.freezeapp.daemon.clipboard.IDaemonClipboardChange;
import com.john.freezeapp.daemon.clipboard.IDaemonClipboardMonitorBinder;
import com.john.freezeapp.monitor.AppMonitorService;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.SharedPrefUtil;

import java.util.List;

public class Clipboard {
    public static void start() {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                daemonClipboardMonitorBinder.startMonitor();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void stop() {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                daemonClipboardMonitorBinder.endMonitor();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static List<ClipboardData> getClipData() {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                return daemonClipboardMonitorBinder.getClipboardData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean isMonitor() {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                return daemonClipboardMonitorBinder.isMonitor();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }


    public static void remove(String id) {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                daemonClipboardMonitorBinder.removeClipboardData(id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear() {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                daemonClipboardMonitorBinder.clearClipboardData();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean copy(String id) {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                return daemonClipboardMonitorBinder.setClipboardData(id);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static void addClipboardChange(ClipboardChange change) {
        IDaemonClipboardMonitorBinder daemonClipboardMonitorBinder = getDaemonClipboardMonitorBinder();
        if (daemonClipboardMonitorBinder != null) {
            try {
                daemonClipboardMonitorBinder.addClipboardDataChange(change);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    abstract static class ClipboardChange extends IDaemonClipboardChange.Stub {

    }


    public static IDaemonClipboardMonitorBinder getDaemonClipboardMonitorBinder() {
        try {
            IBinder service = ClientBinderManager.getDaemonBinder().getService(DaemonHelper.DAEMON_BINDER_CLIPBOARD_MONITOR);
            if (service != null) {
                return IDaemonClipboardMonitorBinder.Stub.asInterface(service);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static boolean isClipboardFloating() {
        return SharedPrefUtil.getBoolean(SharedPrefUtil.KEY_CLIPBOARD_FLOAT_SWITCHER, false);
    }

    public static void setClipboardFloating(boolean isOpen) {
        SharedPrefUtil.setBoolean(SharedPrefUtil.KEY_CLIPBOARD_FLOAT_SWITCHER, isOpen);
    }


    public static void startClipboardFloating(Context context) {
        if (ClientBinderManager.isActive()) {
            if (isClipboardFloating() && FreezeUtil.isOverlayPermission(context)) {
                ClipboardService.startClipboardFloating(context);
            }
        }
    }

    public static void stopClipboardFloating(Context context) {
        ClipboardService.stopClipboardFloating(context);
    }

}
