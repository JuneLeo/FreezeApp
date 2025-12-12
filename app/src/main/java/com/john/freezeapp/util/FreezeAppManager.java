package com.john.freezeapp.util;

import static com.john.freezeapp.util.PackageUtil.STATUS_ALL;
import static com.john.freezeapp.util.PackageUtil.STATUS_DISABLE_APP;
import static com.john.freezeapp.util.PackageUtil.STATUS_ENABLE_APP;
import static com.john.freezeapp.util.PackageUtil.TYPE_NORMAL_APP;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.text.TextUtils;

import com.john.freezeapp.BuildConfig;
import com.john.freezeapp.client.ClientSystemService;
import com.john.freezeapp.daemon.CommonShellUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class FreezeAppManager {


    private static final Map<String, AppModel> sAllAppMap = new HashMap<>();

    private static final Map<String, CacheAppModel> sCacheAppModel = new ConcurrentHashMap<>();

    public static class CacheAppModel {
        public String packageName;
        public String name;
        public Drawable icon;
    }

    public static class AppModel {
        public String packageName;
        public long lastUpdateTime;
        public long firstInstallTime;
        public String versionName;
        public int versionCode;

        public AppModel(String packageName) {
            this.packageName = packageName;
        }

        public AppModel(AppModel appModel) {
            this.packageName = appModel.packageName;
            this.lastUpdateTime = appModel.lastUpdateTime;
            this.firstInstallTime = appModel.firstInstallTime;
            this.versionName = appModel.versionName;
            this.versionCode = appModel.versionCode;
        }

    }

    public static class ProcessModel {
        // UID            PID  PPID C STIME TTY          TIME CMD
        //
        public String uid;
        public String pid;
        public String ppid;
        public String c;
        public String tty;
        public String packageName;
        public String processName;
        public String sTime;
        public String time;


    }

    public static class RunningModel extends AppModel {
        public List<ProcessModel> processModels = new ArrayList<>();

        public RunningModel(AppModel appModel) {
            super(appModel);
        }

        public void addProcess(ProcessModel processModel) {
            processModels.add(processModel);
        }
    }

    public interface AppModelCallback {
        void success(List<AppModel> list);

        void fail();
    }


    public interface ActionCallback {
        void success();

        void fail();
    }

    public interface RunningCallback {
        void success(List<RunningModel> list);

        void fail();
    }


    public static void requestForceStopApp(String packageName, ActionCallback callback) {
        ThreadPool.execute(() -> {
            try {
                ClientSystemService.getActivityManager().forceStopPackage(packageName, 0);
                if (callback != null) {
                    callback.success();
                }
            } catch (RemoteException e) {
                if (callback != null) {
                    callback.fail();
                }
            }
        });

    }


    public static void requestDefrostApp(String packageName, ActionCallback callback) {
        ThreadPool.execute(() -> {
            try {
                ClientSystemService.getPackageManager().setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0, 0, "");
                callback.success();
            } catch (RemoteException e) {
                e.printStackTrace();
                callback.fail();
            }
        });

    }


    public static void requestFreezeApp(String packageName, ActionCallback callback) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    ClientSystemService.getPackageManager().setApplicationEnabledSetting(packageName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER, 0, 0, "");
                    callback.success();
                } catch (RemoteException e) {
                    e.printStackTrace();
                    callback.fail();
                }
            }
        });
    }

    public static void requestEnableApp(Context context, AppModelCallback callback) {
        requestAppList(context, TYPE_NORMAL_APP, STATUS_ENABLE_APP, true, callback);
    }

    public static void requestDisableApp(Context context, AppModelCallback callback) {
        requestAppList(context, TYPE_NORMAL_APP, STATUS_DISABLE_APP, true, callback);
    }

    public static void requestAllUserApp(Context context, AppModelCallback callback) {
        requestAppList(context, TYPE_NORMAL_APP, STATUS_ALL, false, callback);
    }

    public static void requestAppList(Context context, @PackageUtil.TYPE int appType, @PackageUtil.STATUS int appStatus, boolean ignoreFreezeApp, AppModelCallback callback) {
        ThreadPool.execute(() -> {
            List<AppModel> install = getInstallAppModel(appType, appStatus, ignoreFreezeApp);
            if (install != null) {
                callback.success(install);
            } else {
                callback.fail();
            }
        });
    }

    public static List<AppModel> getInstallAppModel(@PackageUtil.TYPE int appType, @PackageUtil.STATUS int appStatus, boolean ignoreFreezeApp) {
        List<PackageInfo> packageInfos = getInstallApp(appType, appStatus, ignoreFreezeApp);
        if (packageInfos != null) {
            List<AppModel> list = new ArrayList<>();
            for (PackageInfo packageInfo : packageInfos) {

                AppModel appModel = new AppModel(packageInfo.packageName);
                appModel.lastUpdateTime = packageInfo.lastUpdateTime;
                appModel.firstInstallTime = packageInfo.firstInstallTime;
                appModel.versionName = packageInfo.versionName;
                appModel.versionCode = packageInfo.versionCode;
                list.add(appModel);
            }
            return list;
        }
        return null;
    }


    public static void requestRunningApp(Context context, RunningCallback callback) {
        requestRunningApp(context, false, callback);
    }

    public static void requestRunningApp(Context context, boolean force, RunningCallback callback) {
        if (force) {
            sAllAppMap.clear();
        }
        if (!sAllAppMap.isEmpty()) {
            requestRunningProcess2(context, callback);
        } else {

            requestAppList(context, TYPE_NORMAL_APP, STATUS_ENABLE_APP, true, new AppModelCallback() {
                @Override
                public void success(List<AppModel> list) {
                    sAllAppMap.clear();
                    for (AppModel appModel : list) {
                        sAllAppMap.put(appModel.packageName, appModel);
                    }
                    requestRunningProcess2(context, callback);
                }

                @Override
                public void fail() {

                }
            });
        }
    }

    private static void requestRunningProcess2(Context context, RunningCallback callback) {
        ThreadPool.execute(() -> {
            try {
                List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = ClientSystemService.getActivityManager().getRunningAppProcesses();
                Map<String, RunningModel> runningModelMap = new HashMap<>();
                for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses) {
                    ProcessModel processModel = new ProcessModel();
                    processModel.processName = runningAppProcess.processName;
                    processModel.pid = runningAppProcess.pid + "";
                    for (String packageName : runningAppProcess.pkgList) {
                        if (!TextUtils.equals(packageName, BuildConfig.APPLICATION_ID)) {
                            AppModel appModel = sAllAppMap.get(packageName);
                            if (appModel != null) {
                                RunningModel runningModel = runningModelMap.get(packageName);
                                if (runningModel == null) {
                                    runningModel = new RunningModel(appModel);
                                    runningModelMap.put(packageName, runningModel);
                                }
                                processModel.packageName = appModel.packageName;
                                runningModel.addProcess(processModel);
                            }
                        }
                    }
                }

                List<RunningModel> runningModels = new ArrayList<>();

                for (Map.Entry<String, RunningModel> entry : runningModelMap.entrySet()) {
                    runningModels.add(entry.getValue());
                }
                callback.success(runningModels);

            } catch (RemoteException e) {
                e.printStackTrace();
                callback.fail();
            }
        });
    }

    public static CacheAppModel getAppModel(Context context, String packageName) {
        return getAppModel(context, packageName, false);
    }

    public static CacheAppModel getAppModel(Context context, String packageName, boolean onlyCache) {
        CacheAppModel appModel = new CacheAppModel();
        appModel.packageName = packageName;

        CacheAppModel cacheAppModel = sCacheAppModel.get(packageName);

        if (onlyCache) {
            if (cacheAppModel == null) {
                return null;
            }
        }

        if (cacheAppModel == null) {
            synchronized (sCacheAppModel) {
                cacheAppModel = sCacheAppModel.get(packageName);
                if (cacheAppModel == null) {
                    cacheAppModel = new CacheAppModel();
                    try {
                        ApplicationInfo applicationInfo = getApplicationInfo(packageName);
                        cacheAppModel.name = applicationInfo.loadLabel(context.getPackageManager()).toString();
                        cacheAppModel.icon = applicationInfo.loadIcon(context.getPackageManager());
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    sCacheAppModel.put(packageName, cacheAppModel);
                }
            }
        }

        appModel.name = cacheAppModel.name;
        appModel.icon = cacheAppModel.icon;

        return appModel;
    }

    private static ApplicationInfo getApplicationInfo(String packageName) throws RemoteException {
        IPackageManager iPackageManager = ClientSystemService.getPackageManager();
        ApplicationInfo applicationInfo;
        if (DeviceUtil.atLeast33()) {
            applicationInfo = iPackageManager.getApplicationInfo(packageName, 0L, 0);
        } else {
            applicationInfo = iPackageManager.getApplicationInfo(packageName, 0, 0);
        }
        return applicationInfo;
    }

    public static List<PackageInfo> getInstallUserApp() {
        return getInstallApp(TYPE_NORMAL_APP, STATUS_ALL, false);
    }

    public static List<PackageInfo> getInstallApp(@PackageUtil.TYPE int appType, @PackageUtil.STATUS int appStatus, boolean ignoreFreezeApp) {
        try {
            ParceledListSlice<PackageInfo> installedPackages = null;
            if (DeviceUtil.atLeast33()) {
                installedPackages = ClientSystemService.getPackageManager().getInstalledPackages(0L, 0);
            } else {
                installedPackages = ClientSystemService.getPackageManager().getInstalledPackages(0, 0);
            }
            if (installedPackages != null) {
                return PackageUtil.filterApp(installedPackages.getList(), appType, appStatus, ignoreFreezeApp);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void toRoot(Context context) {
        CommonShellUtils.execCommand(FreezeUtil.getStartDaemonShell(context), true, new CommonShellUtils.ShellCommandResultCallback() {
            @Override
            public void callback(CommonShellUtils.ShellCommandResult commandResult) {

            }
        });
    }


}
