package com.john.freezeapp.monitor;

import android.app.ActivityManager;
import android.app.ActivityManagerHidden;
import android.app.ITaskStackListener;
import android.content.ComponentName;
import android.window.TaskSnapshot;

import com.john.freezeapp.util.CommonLog;

public class TaskStackListener extends ITaskStackListener.Stub {
    @Override
    public void onTaskStackChanged() {
        CommonLog.log("AppMonitorManager onTaskStackChanged");
    }

    @Override
    public void onActivityPinned(String packageName, int userId, int taskId, int stackId) {
        CommonLog.log(String.format("AppMonitorManager onTaskStackChanged packageName=%s,userId=%d,taskId=%d,stackId=%d", packageName, userId, taskId, stackId));
    }

    @Override
    public void onActivityUnpinned() {
        CommonLog.log("AppMonitorManager onActivityUnpinned");
    }

    @Override
    public void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible, boolean clearedTask, boolean wasVisible) {
        CommonLog.log(String.format("AppMonitorManager onActivityRestartAttempt task=%s,userId=%b,taskId=%b,stackId=%b", task.toString(), homeTaskVisible, clearedTask, wasVisible));

    }

    @Override
    public void onActivityForcedResizable(String packageName, int taskId, int reason) {
        CommonLog.log(String.format("AppMonitorManager onActivityForcedResizable packageName=%s,taskId=%d,reason=%d", packageName, taskId, reason));
    }

    @Override
    public void onActivityDismissingDockedTask() {
        CommonLog.log("AppMonitorManager onActivityDismissingDockedTask");
    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) {
        CommonLog.log(String.format("AppMonitorManager onActivityLaunchOnSecondaryDisplayFailed taskInfo=%s,userId=%d", taskInfo.toString(), requestedDisplayId));

    }

    @Override
    public void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo, int requestedDisplayId) {
        CommonLog.log(String.format("AppMonitorManager onActivityLaunchOnSecondaryDisplayRerouted taskInfo=%s,userId=%d", taskInfo.toString(), requestedDisplayId));

    }

    @Override
    public void onTaskCreated(int taskId, ComponentName componentName) {
        CommonLog.log(String.format("AppMonitorManager onTaskCreated taskId=%d", taskId));
    }

    @Override
    public void onTaskRemoved(int taskId) {
        CommonLog.log(String.format("AppMonitorManager onTaskRemoved taskId=%d", taskId));
    }

    @Override
    public void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onTaskMovedToFront taskInfo=%s", taskInfo.toString()));
        ComponentName topActivity = taskInfo.topActivity;
        if (topActivity != null) {
            String className = topActivity.getClassName();
            CommonLog.log(String.format("AppMonitorManager topActivityRecord=%s", className));
        }
    }

    @Override
    public void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onTaskDescriptionChanged taskInfo=%s", taskInfo.toString()));
        ComponentName topActivity = taskInfo.topActivity;
        if (topActivity != null) {
            String className = topActivity.getClassName();
            CommonLog.log(String.format("AppMonitorManager topActivityRecord=%s", className));
        }

    }

    @Override
    public void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation) {
        CommonLog.log(String.format("AppMonitorManager onActivityRequestedOrientationChanged taskId=%d,requestedOrientation=%d", taskId, requestedOrientation));
    }

    @Override
    public void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onTaskRemovalStarted taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onTaskProfileLocked taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot) {
        CommonLog.log(String.format("AppMonitorManager onTaskSnapshotChanged taskId=%d,snapshot=%s", taskId, snapshot.toString()));
    }

    @Override
    public void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onBackPressedOnTaskRoot taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onTaskDisplayChanged(int taskId, int newDisplayId) {
        CommonLog.log(String.format("AppMonitorManager onTaskSnapshotChanged taskId=%d,newDisplayId=%d", taskId, newDisplayId));
    }

    @Override
    public void onRecentTaskListUpdated() {
        CommonLog.log("AppMonitorManager onRecentTaskListUpdated");
    }

    @Override
    public void onRecentTaskListFrozenChanged(boolean frozen) {
        CommonLog.log(String.format("AppMonitorManager onRecentTaskListFrozenChanged frozen=%b", frozen));
    }

    @Override
    public void onTaskFocusChanged(int taskId, boolean focused) {
        CommonLog.log(String.format("AppMonitorManager onTaskFocusChanged taskId=%d,newDisplayId=%b", taskId, focused));
    }

    @Override
    public void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation) {
        CommonLog.log(String.format("AppMonitorManager onTaskRequestedOrientationChanged taskId=%d,newDisplayId=%d", taskId, requestedOrientation));
    }

    @Override
    public void onActivityRotation(int displayId) {
        CommonLog.log(String.format("AppMonitorManager onActivityRotation displayId=%d", displayId));
    }

    @Override
    public void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo) {
        CommonLog.log(String.format("AppMonitorManager onTaskMovedToBack taskInfo=%s", taskInfo.toString()));
    }

    @Override
    public void onLockTaskModeChanged(int mode) {
        CommonLog.log(String.format("AppMonitorManager onLockTaskModeChanged displayId=%d", mode));
    }

    @Override
    public void onTaskSnapshotChanged(int taskId, ActivityManagerHidden.TaskSnapshot snapshot) {

    }

    @Override
    public void onRecentTaskRemovedForAddTask(int taskId) {

    }
}
