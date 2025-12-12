package com.john.freezeapp.daemon;

import android.app.ActivityManagerNative;
import android.app.ContentProviderHolder;
import android.app.IActivityManager;
import android.app.IActivityManagerPre26;
import android.app.IProcessObserver;
import android.content.AttributionSource;
import android.content.Context;
import android.content.IContentProvider;
import android.content.pm.IPackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.text.TextUtils;

import com.john.freezeapp.IDaemonBinder;
import com.john.freezeapp.daemon.util.DaemonUtil;
import com.john.freezeapp.util.DeviceUtil;
import com.john.hidden.api.ReplaceRef;

import java.util.ArrayList;
import java.util.List;

public class DaemonBinderManager {

    private static final IDaemonBinder sBinderContainer = new DaemonBinderStub();


    public static IDaemonBinder getBinderContainer() {
        return sBinderContainer;
    }

    private static final IProcessObserver.Stub iProcessObserverStub = new IProcessObserver.Stub() {

        private final List<Integer> PID_LIST = new ArrayList<>();

        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
            synchronized (PID_LIST) {
                if (PID_LIST.contains(pid) || !foregroundActivities) {
                    return;
                }
                PID_LIST.add(pid);
            }
            DaemonLog.log("onForegroundActivitiesChanged - pid=" + pid + ",foregroundActivities=" + foregroundActivities);
            sendBinder(uid, pid);
        }

        @Override
        public void onProcessDied(int pid, int uid) throws RemoteException {
            synchronized (PID_LIST) {
                int index = PID_LIST.indexOf(pid);
                if (index != -1) {
                    PID_LIST.remove(index);
                }
            }

            DaemonLog.log("onProcessDied - pid=" + pid);
        }

        @Override
        public void onProcessStateChanged(int pid, int uid, int procState) throws RemoteException {
            synchronized (PID_LIST) {
                if (PID_LIST.contains(pid)) {
                    return;
                }
                PID_LIST.add(pid);
            }
            DaemonLog.log("onProcessStateChanged - pid=" + pid + ",procState=" + procState);
            sendBinder(uid, pid);
        }

        @Override
        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {

        }

        @Override
        public void onProcessStarted(int pid, int processUid, int packageUid, String packageName, String processName) throws RemoteException {
            DaemonLog.log("onProcessStateChanged - pid=" + pid + ",uid=" + processUid + ",packageUid=" + packageUid + ",packageName=" + packageName + ",processName=" + processName);
        }


    };

    public static void sendBinder(int uid, int pid) {
        try {
            DaemonLog.log("sendBinder start");
            if (isFreezeApp(uid)) {
                DaemonLog.log("sendBinder sendBinderContainer");
                sendBinderContainer();
            }
        } catch (Throwable e) {
            DaemonLog.e(e, "isFreezeApp");
        }
    }

    public static void sendBinderContainer() {
        try {
            String name = "com.john.freezeapp.daemon";
            DaemonLog.log("sendBinderContainer start");
            IContentProvider contentProvider = getContentProviderExternal(name, 0, null, name);
            DaemonLog.log("sendBinderContainer step 1");
            if (contentProvider == null) {
                DaemonLog.log("sendBinder contentProvider = null");
                return;
            }
            Bundle extras = new Bundle();
            extras.putBinder("binder", sBinderContainer.asBinder());
            DaemonLog.log("sendBinderContainer step 2");
            String callingPackageName = DaemonUtil.getDaemonPackageName();
            Bundle reply = callCompat(contentProvider, callingPackageName, name, "sendBinder", null, extras);
            DaemonLog.log("sendBinderContainer step 3");
        } catch (Throwable e) {
            DaemonLog.e(e, "sendBinderContainer");
        }
    }


    private static boolean isFreezeApp(int uid) throws RemoteException {
        DaemonLog.log("isFreezeApp start");
        String[] packages = DaemonService.getPackageManager().getPackagesForUid(uid);
        DaemonLog.log("isFreezeApp packages");
        for (String aPackage : packages) {
            if (TextUtils.equals(aPackage, BuildConfig.CLIENT_PACKAGE)) {
                return true;
            }
        }
        return false;
    }

    public static IContentProvider getContentProviderExternal( String name, int userId,  IBinder token,  String tag) throws RemoteException {
        IActivityManager am = DaemonService.getActivityManager();
        ContentProviderHolder contentProviderHolder;
        IContentProvider provider;
        if (DeviceUtil.atLeast29()) {
            contentProviderHolder = am.getContentProviderExternal(name, userId, token, tag);
            provider = contentProviderHolder != null ? contentProviderHolder.provider : null;
            DaemonLog.log("contentProviderHolder=" + contentProviderHolder);
        } else if (DeviceUtil.atLeast26()) {
            contentProviderHolder = am.getContentProviderExternal(name, userId, token);
            provider = contentProviderHolder != null ? contentProviderHolder.provider : null;
        } else {
            provider = ReplaceRef.<IActivityManagerPre26>unsafeCast(am).getContentProviderExternal(name, userId, token).provider;
        }

        return provider;
    }


    public static Bundle callCompat(IContentProvider provider,  String callingPkg,  String authority,  String method,  String arg,  Bundle extras) throws RemoteException {
        Bundle result;
        if (DeviceUtil.atLeast31()) {
            result = provider.call((new AttributionSource.Builder(android.system.Os.getuid())).setPackageName(callingPkg).build(), authority, method, arg, extras);
        } else if (DeviceUtil.atLeast30()) {
            result = provider.call(callingPkg, (String) null, authority, method, arg, extras);
        } else if (DeviceUtil.atLeast29()) {
            result = provider.call(callingPkg, authority, method, arg, extras);
        } else {
            result = provider.call(callingPkg, method, arg, extras);
        }

        return result;
    }


    private static Context sContext;

    public static void register(Context context) {
        sContext = context;
        try {
            DaemonService.getActivityManager().registerProcessObserver(iProcessObserverStub);
        } catch (Throwable e) {
            //
        }
    }

    public static void unregister() {
        try {
            DaemonService.getActivityManager().unregisterProcessObserver(iProcessObserverStub);
        } catch (Throwable e) {
            //
        }
    }
}
