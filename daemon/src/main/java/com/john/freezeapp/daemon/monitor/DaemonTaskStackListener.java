package com.john.freezeapp.daemon.monitor;

import android.app.ActivityManager;
import android.app.ActivityManagerHidden;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.window.TaskSnapshot;
import com.john.freezeapp.daemon.DaemonLog;

public class DaemonTaskStackListener extends ITaskStackListener.Stub {
    @Override
    public void onTaskStackChanged() {
        DaemonLog.log("AppMonitorManager onTaskStackChanged");
    }

    @Override
    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) {
        DaemonLog.log(String.format("AppMonitorManager onTaskStackChanged packageName=%s,userId=%d,taskId=%d,stackId=%d", packageName, userId, taskId, stackId));
    }

    @Override
    public void onActivityUnpinned() {
        DaemonLog.log("AppMonitorManager onActivityUnpinned");
    }

    @Override
    public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible, boolean clearedTask, boolean wasVisible) {
        DaemonLog.log(String.format("AppMonitorManager onActivityRestartAttempt task=%s,userId=%b,taskId=%b,stackId=%b", task.toString(), homeTaskVisible, clearedTask, wasVisible));

    }

    @Override
    public void onActivityForcedResizable(String packageName, int taskId, int reason) {
        DaemonLog.log(String.format("AppMonitorManager onActivityForcedResizable packageName=%s,taskId=%d,reason=%d", packageName, taskId, reason));
    }

    @Override
    public void onActivityDismissingDockedTask() {
        DaemonLog.log("AppMonitorManager onActivityDismissingDockedTask");
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) {
        DaemonLog.log(String.format("AppMonitorManager onActivityLaunchOnSecondaryDisplayFailed taskInfo=%s,userId=%d", taskInfo.toString(), requestedDisplayId));

    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) {
        DaemonLog.log(String.format("AppMonitorManager onActivityLaunchOnSecondaryDisplayRerouted taskInfo=%s,userId=%d", taskInfo.toString(), requestedDisplayId));

    }

    @Override
    public void onTaskCreated(int taskId, ComponentName componentName) {
        DaemonLog.log(String.format("AppMonitorManager onTaskCreated taskId=%d", taskId));
    }

    @Override
    public void onTaskRemoved(int taskId) {
        DaemonLog.log(String.format("AppMonitorManager onTaskRemoved taskId=%d", taskId));
    }

    @Override
    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onTaskMovedToFront taskInfo=%s", taskInfo.toString()));
        ComponentName topActivity = taskInfo.topActivity;
        if (topActivity != null) {
            String className = topActivity.getClassName();
            DaemonLog.log(String.format("AppMonitorManager topActivityRecord=%s", className));
        }
    }

    @Override
    public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onTaskDescriptionChanged taskInfo=%s", taskInfo.toString()));
        ComponentName topActivity = taskInfo.topActivity;
        if (topActivity != null) {
            String className = topActivity.getClassName();
            DaemonLog.log(String.format("AppMonitorManager topActivityRecord=%s", className));
        }

    }

    @Override
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) {
        DaemonLog.log(String.format("AppMonitorManager onActivityRequestedOrientationChanged taskId=%d,requestedOrientation=%d", taskId, requestedOrientation));
    }

    @Override
    public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onTaskRemovalStarted taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onTaskProfileLocked taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) {
        DaemonLog.log(String.format("AppMonitorManager onTaskSnapshotChanged taskId=%d,snapshot=%s", taskId, snapshot.toString()));
    }

    @Override
    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onBackPressedOnTaskRoot taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskDisplayChanged(int taskId, int newDisplayId) {
        DaemonLog.log(String.format("AppMonitorManager onTaskSnapshotChanged taskId=%d,newDisplayId=%d", taskId, newDisplayId));
    }

    @Override
    public void onRecentTaskListUpdated() {
        DaemonLog.log("AppMonitorManager onRecentTaskListUpdated");
    }

    @Override
    public void onRecentTaskListFrozenChanged(boolean frozen) {
        DaemonLog.log(String.format("AppMonitorManager onRecentTaskListFrozenChanged frozen=%b", frozen));
    }

    @Override
    public void onTaskFocusChanged(int taskId, boolean focused) {
        DaemonLog.log(String.format("AppMonitorManager onTaskFocusChanged taskId=%d,newDisplayId=%b", taskId, focused));
    }

    @Override
    public void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation) {
        DaemonLog.log(String.format("AppMonitorManager onTaskRequestedOrientationChanged taskId=%d,newDisplayId=%d", taskId, requestedOrientation));
    }

    @Override
    public void onActivityRotation(int displayId) {
        DaemonLog.log(String.format("AppMonitorManager onActivityRotation displayId=%d", displayId));
    }

    @Override
    public void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo) {
        DaemonLog.log(String.format("AppMonitorManager onTaskMovedToBack taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onLockTaskModeChanged(int mode) {
        DaemonLog.log(String.format("AppMonitorManager onLockTaskModeChanged displayId=%d", mode));
    }

    @Override
    public void onTaskSnapshotChanged(int taskId, ActivityManagerHidden.TaskSnapshot snapshot) {

    }

    @Override
    public void onRecentTaskRemovedForAddTask(int taskId) {

    }
}