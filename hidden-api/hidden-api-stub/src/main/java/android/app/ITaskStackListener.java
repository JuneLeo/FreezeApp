package android.app;

import android.content.ComponentName;
import android.os.Binder;
import android.window.TaskSnapshot;

public interface ITaskStackListener {


    /**
     * Activity was resized to be displayed in split-screen.
     */
    int FORCED_RESIZEABLE_REASON_SPLIT_SCREEN = 1;
    /**
     * Activity was resized to be displayed on a secondary display.
     */
    int FORCED_RESIZEABLE_REASON_SECONDARY_DISPLAY = 2;

    void onTaskStackChanged();

    void onActivityPinned(String packageName, int userId, int taskId, int stackId);

    void onActivityUnpinned();


    void onActivityRestartAttempt(ActivityManager.RunningTaskInfo task, boolean homeTaskVisible,
                                  boolean clearedTask, boolean wasVisible);

    void onActivityForcedResizable(String packageName, int taskId, int reason);

    void onActivityDismissingDockedTask();

    void onActivityLaunchOnSecondaryDisplayFailed(ActivityManager.RunningTaskInfo taskInfo,
                                                  int requestedDisplayId);

    void onActivityLaunchOnSecondaryDisplayRerouted(ActivityManager.RunningTaskInfo taskInfo,
                                                    int requestedDisplayId);

    void onTaskCreated(int taskId, ComponentName componentName);

    /**
     * Called when a task is removed.
     *
     * @param taskId id of the task.
     */
    void onTaskRemoved(int taskId);

    void onTaskMovedToFront(ActivityManager.RunningTaskInfo taskInfo);

    void onTaskDescriptionChanged(ActivityManager.RunningTaskInfo taskInfo);

    void onActivityRequestedOrientationChanged(int taskId, int requestedOrientation);

    void onTaskRemovalStarted(ActivityManager.RunningTaskInfo taskInfo);

    /**
     * Called when the task has been put in a locked state because one or more of the
     * activities inside it belong to a managed profile user, and that user has just
     * been locked.
     */
    void onTaskProfileLocked(ActivityManager.RunningTaskInfo taskInfo);

    /**
     * Called when a task snapshot got updated.
     */
    void onTaskSnapshotChanged(int taskId, TaskSnapshot snapshot);

    void onBackPressedOnTaskRoot(ActivityManager.RunningTaskInfo taskInfo);

    void onTaskDisplayChanged(int taskId, int newDisplayId);


    void onRecentTaskListUpdated();

    /**
     * Called when Recent Tasks list is frozen or unfrozen.
     *
     * @param frozen if true, Recents Tasks list is currently frozen, false otherwise
     */
    void onRecentTaskListFrozenChanged(boolean frozen);

    /**
     * Called when a task gets or loses focus.
     *
     * @param taskId id of the task.
     * @param {@code true} if the task got focus, {@code false} if it lost it.
     */
    void onTaskFocusChanged(int taskId, boolean focused);

    /**
     * Called when a task changes its requested orientation. It is different from {@link
     * #onActivityRequestedOrientationChanged(int, int)} in the sense that this method is called
     * when a task changes requested orientation due to activity launch, dimiss or reparenting.
     *
     * @param taskId               id of the task.
     * @param requestedOrientation the new requested orientation of this task as screen orientations
     *                             in {@link android.content.pm.ActivityInfo}.
     */
    void onTaskRequestedOrientationChanged(int taskId, int requestedOrientation);

    /**
     * Called when a rotation is about to start on the foreground activity.
     * This applies for:
     * * free sensor rotation
     * * forced rotation
     * * rotation settings set through adb command line
     * * rotation that occurs when rotation tile is toggled in quick settings
     *
     * @param displayId id of the display where activity will rotate
     */
    void onActivityRotation(int displayId);


    void onTaskMovedToBack(ActivityManager.RunningTaskInfo taskInfo);

    /**
     * Called when the lock task mode changes. See ActivityManager#LOCK_TASK_MODE_* and
     * LockTaskController.
     */
    void onLockTaskModeChanged(int mode);


    void onTaskSnapshotChanged(int taskId, ActivityManagerHidden.TaskSnapshot snapshot);


    void onRecentTaskRemovedForAddTask(int taskId);

    abstract class Stub extends Binder implements ITaskStackListener {

    }
}
