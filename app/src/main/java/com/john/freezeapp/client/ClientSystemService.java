package com.john.freezeapp.client;

import android.annotation.TargetApi;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.smartspace.ISmartspaceManager;
import android.app.usage.IStorageStatsManager;
import android.app.usage.IUsageStatsManager;
import android.content.Context;
import android.content.IClipboard;
import android.content.pm.IPackageManager;
import android.hardware.display.IDisplayManager;
import android.net.INetworkStatsService;
import android.os.Build;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.IInstalld;
import android.os.storage.IStorageManager;
import android.permission.IPermissionManager;
import android.view.IWindowManager;

import com.android.internal.app.IAppOpsService;
import com.android.internal.app.IBatteryStats;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.FreezeUtil;

public class ClientSystemService {


    private static boolean isActive() {
        return ClientBinderManager.isActive();
    }


    private final static ClientBinderSingleton<IPackageManager> iPackageManager = new ClientBinderSingleton<IPackageManager>() {
        @Override
        protected IPackageManager createBinder() {
            if (!isActive()) {
                return null;
            }

            return IPackageManager.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService("package")));
        }
    };


    private final static ClientBinderSingleton<IActivityManager> iActivityManager = new ClientBinderSingleton<IActivityManager>() {
        @Override
        protected IActivityManager createBinder() {
            if (!isActive()) {
                return null;
            }
            IBinder binder = new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService(Context.ACTIVITY_SERVICE));
            if (DeviceUtil.atLeast26()) {
                return IActivityManager.Stub.asInterface(binder);
            } else {
                return ActivityManagerNative.asInterface(binder);
            }
        }
    };


    private final static ClientBinderSingleton<IBatteryStats> iBatteryStats = new ClientBinderSingleton<IBatteryStats>() {
        @Override
        protected IBatteryStats createBinder() {
            if (!isActive()) {
                return null;
            }
            try {
                return IBatteryStats.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService("batterystats")));
            } catch (Throwable e) {
                return null;
            }
        }
    };

    private final static ClientBinderSingleton<IUsageStatsManager> iUsageStatsManager = new ClientBinderSingleton<IUsageStatsManager>() {
        @Override
        protected IUsageStatsManager createBinder() {
            if (!isActive()) {
                return null;
            }
            return IUsageStatsManager.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService(Context.USAGE_STATS_SERVICE)));
        }
    };


    private final static ClientBinderSingleton<ISmartspaceManager> iSmartspaceManager = new ClientBinderSingleton<ISmartspaceManager>() {
        @Override
        protected ISmartspaceManager createBinder() {

            IBinder systemService = SystemServiceHelper.getSystemService("smartspace");
            if (systemService != null) {
                return ISmartspaceManager.Stub.asInterface(new ClientSystemBinderWrapper(systemService));
            }
            return null;
        }
    };

    private final static ClientBinderSingleton<IWindowManager> iWindowManager = new ClientBinderSingleton<IWindowManager>() {
        @Override
        protected IWindowManager createBinder() {

            if (!isActive()) {
                return null;
            }

            IBinder systemService = SystemServiceHelper.getSystemService("window");
            if (systemService != null) {
                return IWindowManager.Stub.asInterface(new ClientSystemBinderWrapper(systemService));
            }
            return null;
        }
    };


    private final static ClientBinderSingleton<IAppOpsService> iAppOpsService = new ClientBinderSingleton<IAppOpsService>() {
        @Override
        protected IAppOpsService createBinder() {
            if (!isActive()) {
                return null;
            }

            return IAppOpsService.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService(Context.APP_OPS_SERVICE)));
        }
    };


    private final static ClientBinderSingleton<IPermissionManager> iPermissionManager = new ClientBinderSingleton<IPermissionManager>() {
        @TargetApi(Build.VERSION_CODES.R)
        @Override
        protected IPermissionManager createBinder() {
            if (!isActive()) {
                return null;
            }
            return IPermissionManager.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService("permissionmgr")));
        }
    };

    private final static ClientBinderSingleton<IStorageManager> iStorageManager = new ClientBinderSingleton<IStorageManager>() {
        @Override
        protected IStorageManager createBinder() {
            if (!isActive()) {
                return null;
            }

            return IStorageManager.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService("mount")));
        }
    };

    private final static ClientBinderSingleton<IStorageStatsManager> iStorageStatsManager = new ClientBinderSingleton<IStorageStatsManager>() {
        @TargetApi(Build.VERSION_CODES.O)
        @Override
        protected IStorageStatsManager createBinder() {
            if (!isActive()) {
                return null;
            }

            return IStorageStatsManager.Stub.asInterface(new ClientSystemBinderWrapper(SystemServiceHelper.getSystemService(Context.STORAGE_STATS_SERVICE)));
        }
    };


    private final static ClientBinderSingleton<IInstalld> iInstalld = new ClientBinderSingleton<IInstalld>() {
        @Override
        protected IInstalld createBinder() {
            if (!isActive()) {
                return null;
            }

            IBinder installd = SystemServiceHelper.getSystemService("installd");
            if (installd != null) {
                return IInstalld.Stub.asInterface(new ClientSystemBinderWrapper(installd));
            }

            return null;
        }
    };

    private final static ClientBinderSingleton<IClipboard> iClipboard = new ClientBinderSingleton<IClipboard>() {
        @Override
        protected IClipboard createBinder() {
            if (!isActive()) {
                return null;
            }

            IBinder iBinder = SystemServiceHelper.getSystemService(Context.CLIPBOARD_SERVICE);
            if (iBinder != null) {
                return IClipboard.Stub.asInterface(new ClientSystemBinderWrapper(iBinder));
            }

            return null;
        }
    };

    private final static ClientBinderSingleton<IDeviceIdleController> iDeviceIdleController = new ClientBinderSingleton<IDeviceIdleController>() {
        @Override
        protected IDeviceIdleController createBinder() {
            if (!isActive()) {
                return null;
            }

            IBinder iBinder = SystemServiceHelper.getSystemService("deviceidle");
            if (iBinder != null) {
                return IDeviceIdleController.Stub.asInterface(new ClientSystemBinderWrapper(iBinder));
            }

            return null;
        }
    };

    private final static ClientBinderSingleton<INetworkStatsService> iNetworkStatsService = new ClientBinderSingleton<INetworkStatsService>() {
        @Override
        protected INetworkStatsService createBinder() {
            if (!isActive()) {
                return null;
            }

            IBinder iBinder = SystemServiceHelper.getSystemService(Context.NETWORK_STATS_SERVICE);
            if (iBinder != null) {
                return INetworkStatsService.Stub.asInterface(new ClientSystemBinderWrapper(iBinder));
            }

            return null;
        }
    };

    private final static ClientBinderSingleton<IDisplayManager> iDisplayManager = new ClientBinderSingleton<IDisplayManager>() {
        @Override
        protected IDisplayManager createBinder() {
            if (!isActive()) {
                return null;
            }

            IBinder displayBinder = SystemServiceHelper.getSystemService(Context.DISPLAY_SERVICE);
            if (displayBinder != null) {
                return IDisplayManager.Stub.asInterface(new ClientSystemBinderWrapper(displayBinder));
            }

            return null;
        }
    };

    public static IDisplayManager getDisplayManager() {
        return iDisplayManager.get();
    }

    public static INetworkStatsService getNetworkStatsService() {
        return iNetworkStatsService.get();
    }

    public static IDeviceIdleController getDeviceIdleController() {
        return iDeviceIdleController.get();
    }

    public static IActivityManager getActivityManager() {
        return iActivityManager.get();
    }

    public static IPackageManager getPackageManager() {
        return iPackageManager.get();
    }

    public static IBatteryStats getBatteryStats() {
        return iBatteryStats.get();
    }

    public static IUsageStatsManager getUsageStatsManager() {
        return iUsageStatsManager.get();
    }

    public static ISmartspaceManager getSmartspaceManager() {
        return iSmartspaceManager.get();
    }

    public static IWindowManager getWindowManager() {
        return iWindowManager.get();
    }

    public static IAppOpsService getAppOpsService() {
        return iAppOpsService.get();
    }

    @TargetApi(Build.VERSION_CODES.R)
    public static IPermissionManager getPermissionManager() {
        return iPermissionManager.get();
    }

    public static IStorageManager getStorageManager() {
        return iStorageManager.get();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static IStorageStatsManager getStorageStatsManager() {
        return iStorageStatsManager.get();
    }

    @Deprecated
    public static IInstalld getInstalld() {
        return iInstalld.get();
    }

    public static IClipboard getiClipboard() {
        return iClipboard.get();
    }


}
