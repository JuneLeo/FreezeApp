package com.john.freezeapp.memory;

import android.annotation.TargetApi;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.john.freezeapp.BaseService;
import com.john.freezeapp.R;
import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.clipboard.ClipboardActivity;
import com.john.freezeapp.daemon.DaemonBinderManager;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.daemon.autoglm.IAutoGLMBinder;
import com.john.freezeapp.daemon.memory.IMemoryMonitorBinder;
import com.john.freezeapp.daemon.memory.IMemoryMonitorListener;
import com.john.freezeapp.daemon.memory.MemoryData;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.ScreenUtils;
import com.john.freezeapp.util.SharedPrefUtil;
import com.john.freezeapp.window.FloatWindow;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AppMemoryService extends BaseService {
    private static final String ACTION_START_MEMORY_MONITOR_FLOATING = "action_start_memory_monitor_floating";
    private static final String ACTION_STOP_MEMORY_MONITOR_FLOATING = "action_stop_memory_monitor_floating";
    private static final String ACTION_RESTART_MEMORY_MONITOR_FLOATING = "action_restart_memory_monitor_floating";
    private static final String EXTRA_MEMORY_MONITOR_PID = "extra_memory_monitor_pid";
    private static final String EXTRA_MEMORY_MONITOR_PACKAGE = "extra_memory_monitor_package";
    private static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_MEMORY_MONITOR_FLOATING";
    private static final int notificationId = 1;

    private ScheduledExecutorService sScheduledExecutorService;


    @Override
    public void onCreate() {
        super.onCreate();
        if (DeviceUtil.atLeast26()) {
            createNotificationChannel(getApplicationContext());
        }
        showNotification(getApplicationContext());
        startMemoryMonitorListener();
    }


    @Override
    protected void unbindDaemon() {
        super.unbindDaemon();
        stopSelf();
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, getString(R.string.main_app_memory_monitor_short_title), NotificationManager.IMPORTANCE_LOW);
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationChannel.setAllowBubbles(false);
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        hideWindow();
        stopMemoryMonitorListener();
        stopTask();
    }

    private void stopTask() {
        try {
            IMemoryMonitorBinder memoryMonitorBinder = AppMemoryManager.getMemoryMonitorBinder();
            if (memoryMonitorBinder != null) {
                memoryMonitorBinder.stop();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        String action = intent.getAction();
        if (action == null) {
            return super.onStartCommand(intent, flags, startId);
        }
        switch (action) {
            case ACTION_START_MEMORY_MONITOR_FLOATING:
            case ACTION_RESTART_MEMORY_MONITOR_FLOATING: {
                String packageName = intent.getStringExtra(EXTRA_MEMORY_MONITOR_PACKAGE);
                showWindow(getApplicationContext());
                startMonitor(packageName);
                return START_STICKY;
            }
            case ACTION_STOP_MEMORY_MONITOR_FLOATING: {
                stopSelf();
                return START_NOT_STICKY;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    IMemoryMonitorListener.Stub iMemoryMonitorListener = new IMemoryMonitorListener.Stub() {
        @Override
        public void process(MemoryData data) {
            showAppMemory(data);
        }
    };

    private void startMonitor(String packageName) {
        try {
            IMemoryMonitorBinder memoryMonitorBinder = AppMemoryManager.getMemoryMonitorBinder();
            if (memoryMonitorBinder != null) {
                memoryMonitorBinder.start(packageName, 3000);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void startMemoryMonitorListener() {
        try {
            IMemoryMonitorBinder memoryMonitorBinder = AppMemoryManager.getMemoryMonitorBinder();
            if (memoryMonitorBinder != null) {
                memoryMonitorBinder.addListener(iMemoryMonitorListener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void stopMemoryMonitorListener() {
        try {
            IMemoryMonitorBinder memoryMonitorBinder = AppMemoryManager.getMemoryMonitorBinder();
            if (memoryMonitorBinder != null) {
                memoryMonitorBinder.removeListener(iMemoryMonitorListener);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void showNotification(Context context) {

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        Intent intent = new Intent(context, ClipboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = builder.setColor(getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentTitle(getString(R.string.main_app_memory_monitor))
                .setContentText("")
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                .build();
        try {
            startForeground(notificationId, notification);
        } catch (Throwable e) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    && e instanceof ForegroundServiceStartNotAllowedException) {
                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(notificationId, notification);
            }
        }
    }

    FloatWindow mFloatWindow;
    TextView mTextView;

    private void showWindow(Context context) {
        if (mFloatWindow == null) {
            mFloatWindow = new FloatWindow(context);
            mTextView = new TextView(context);
            int padding = ScreenUtils.dp2px(context, 10);
            mTextView.setPadding(padding, padding, padding, padding);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTextColor(Color.WHITE);
            mTextView.setBackgroundResource(R.drawable.mask_background);
            mFloatWindow.setView(mTextView);

        }
        mFloatWindow.show();
    }


    private void stopSchedule() {
        if (sScheduledExecutorService != null) {
            sScheduledExecutorService.shutdownNow();
            sScheduledExecutorService = null;
        }
    }

    private void showAppMemory(MemoryData memoryData) {
        if (mTextView == null) {
            return;
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Package: " + memoryData.mPackageName + "\n");
        stringBuilder.append("Java Heap: " + memoryData.mJavaHeapPssSize + "\n");
        stringBuilder.append("Native Heap: " + memoryData.mNativeHeapPssSize + "\n");
        stringBuilder.append("Code:" + memoryData.mCodePssSize + "\n");
        stringBuilder.append("Stack: " + memoryData.mStackPssSize + "\n");
        stringBuilder.append("Graphics: " + memoryData.mGraphicsPssSize + "\n");
        stringBuilder.append("Private Other: " + memoryData.mPrivateOtherPssSize + "\n");
        stringBuilder.append("System: " + memoryData.mSystemPssSize + "\n");
        stringBuilder.append("TOTAL PSS: " + memoryData.mTotalPssSize + "\n");
        stringBuilder.append("TOTAL SWAP PSS: " + memoryData.mTotalSwapPssSize + "\n");

        if (mTextView != null) {
            mTextView.post(() -> mTextView.setText(stringBuilder));
        }
    }

    private void hideWindow() {
        if (mFloatWindow != null) {
            mFloatWindow.hide();
        }
    }

    public static void startAppMemoryMonitorFloating(Context context, int pid, String packageName) {
        if (!FreezeUtil.isOverlayPermission(context)) {
            FreezeUtil.allowSystemAlertWindow();
        }
        Intent intent = new Intent(context, AppMemoryService.class);
        intent.setAction(ACTION_START_MEMORY_MONITOR_FLOATING);
        intent.putExtra(EXTRA_MEMORY_MONITOR_PID, pid);
        intent.putExtra(EXTRA_MEMORY_MONITOR_PACKAGE, packageName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopAppMemoryMonitorFloating(Context context) {
        Intent intent = new Intent(context, AppMemoryService.class);
        intent.setAction(ACTION_STOP_MEMORY_MONITOR_FLOATING);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
