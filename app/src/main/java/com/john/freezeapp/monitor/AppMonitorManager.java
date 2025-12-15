package com.john.freezeapp.monitor;

import android.app.ActivityManager;
import android.app.IActivityManager;
import android.app.ITaskStackListener;
import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.client.ClientSystemService;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.daemon.monitor.DaemonAppMonitorConfig;
import com.john.freezeapp.daemon.monitor.IDaemonAppMonitorBinder;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

public class AppMonitorManager {


    /**
     * TaskInfo{userId=0 taskId=857 displayId=0 isRunning=true baseIntent=Intent
     * { act=android.intent.action.MAIN cat=[android.intent.category.HOME]
     * flg=0x10000100 cmp=com.google.android.apps.nexuslauncher/.NexusLauncherActivity }
     * baseActivity=ComponentInfo{com.google.android.googlequicksearchbox/com.google.android.apps.search.googleapp.launcher.minusone.activity.MinusOneActivity}
     * topActivity=ComponentInfo{com.google.android.apps.nexuslauncher/com.google.android.apps.nexuslauncher.NexusLauncherActivity}
     * origActivity=null
     * realActivity=ComponentInfo{com.google.android.apps.nexuslauncher/com.google.android.apps.nexuslauncher.NexusLauncherActivity}
     * numActivities=2
     * lastActiveTime=119435434
     * supportsMultiWindow=true
     * resizeMode=2
     * isResizeable=true
     * minWidth=-1
     * minHeight=-1
     * defaultMinSize=220
     * token=WCT{android.window.IWindowContainerToken$Stub$Proxy@1bd82c9}
     * topActivityType=2
     * pictureInPictureParams=null
     * shouldDockBigOverlays=false
     * launchIntoPipHostTaskId=-1
     * lastParentTaskIdBeforePip=-1
     * displayCutoutSafeInsets=Rect(0, 128 - 0, 0)
     * topActivityInfo=ActivityInfo{c001dce com.google.android.apps.nexuslauncher.NexusLauncherActivity}
     * launchCookies=[]
     * positionInParent=Point(0, 0)
     * parentTaskId=-1
     * isFocused=false
     * isVisible=false
     * isVisibleRequested=false isSleeping=false locusId=LocusId[17_chars]
     * displayAreaFeatureId=1
     * isTopActivityTransparent=false
     * appCompatTaskInfo=AppCompatTaskInfo {
     * topActivityInSizeCompat=false
     * topActivityEligibleForLetterboxEducation= false
     * isLetterboxEducationEnabled= false
     * isLetterboxDoubleTapEnabled= false
     * topActivityEligibleForUserAspectRatioButton= false
     * topActivityBoundsLetterboxed= false
     * isFromLetterboxDoubleTap= false
     * topActivityLetterboxVerticalPosition= -1
     * topActivityLetterboxHorizontalPosition= -1
     * topActivityLetterboxWidth=1080
     * topActivityLetterboxHeight=2400
     * isUserFullscreenOverrideEnabled=false
     * isSystemFullscreenOverrideEnabled=false
     * cameraCompatTaskInfo=CameraCompatTaskInfo { cameraCompatControlState=hidden freeformCameraCompatMode=inactive}}}
     */
    private static ITaskStackListener.Stub sTaskStackListener = null;

    private static void onTaskRunning(ActivityManager.RunningTaskInfo runningTaskInfo) {
        for (IAppMonitor sAppMonitor : sAppMonitors) {
            sAppMonitor.show(runningTaskInfo);
        }
    }


    public static void registerListener() {
        registerTaskStackListener();
        registerOtherListener();
    }

    private static void registerOtherListener() {

    }


    private static void registerTaskStackListener() {
        if (!ClientBinderManager.isActive()) {
            return;
        }

        IActivityManager activityManager = ClientSystemService.getActivityManager();
        if (activityManager != null) {
            try {

                if (sTaskStackListener != null) {
                    activityManager.unregisterTaskStackListener(sTaskStackListener);
                }

                sTaskStackListener = new TaskStackListener() {
                    @Override
                    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) {
                        super.onTaskMovedToFront(taskInfo);
                        onTaskRunning(taskInfo);
                    }

                    @Override
                    public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) {
                        super.onTaskDescriptionChanged(taskInfo);
                        onTaskRunning(taskInfo);
                    }
                };
                activityManager.registerTaskStackListener(sTaskStackListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private static final List<IAppMonitor> sAppMonitors = new ArrayList<>();

    public static void registerAppMonitor(IAppMonitor appMonitor) {
        if (sAppMonitors.contains(appMonitor)) {
            return;
        }
        sAppMonitors.add(appMonitor);
    }

    public static void unregisterAppMonitor(IAppMonitor appMonitor) {
        sAppMonitors.remove(appMonitor);
    }

    public interface IAppMonitor {
        void show(ActivityManager.RunningTaskInfo runningTaskInfo);
    }

    public static boolean isAppMonitor() {
        return SharedPrefUtil.getBoolean(SharedPrefUtil.KEY_APP_MONITOR_SWITCHER, false);
    }

    public static void setAppMonitor(boolean switcher) {
        SharedPrefUtil.setBoolean(SharedPrefUtil.KEY_APP_MONITOR_SWITCHER, switcher);
    }

    public static void startAppMonitor(Context context) {
        if (ClientBinderManager.isActive()) {
            if (isAppMonitor() && FreezeUtil.isOverlayPermission(context)) {
                AppMonitorService.startAppMonitor(context);
            }
        }
    }

    public static void stopAppMonitor(Context context) {
        AppMonitorService.stopAppMonitor(context);
    }

    public static void updateAppMonitorTextSize(Context context) {
        AppMonitorService.updateAppMonitorTextSize(context);
    }

    public static void setTextSize(int size) {
        SharedPrefUtil.setInt(SharedPrefUtil.KEY_APP_MONITOR_TEXT_SIZE, size);
    }

    public static int getTextSize() {
        return SharedPrefUtil.getInt(SharedPrefUtil.KEY_APP_MONITOR_TEXT_SIZE, 12);
    }
}
