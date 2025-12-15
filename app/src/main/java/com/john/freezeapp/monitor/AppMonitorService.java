package com.john.freezeapp.monitor;

import android.annotation.TargetApi;
import android.app.ActivityManager;
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
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.john.freezeapp.App;
import com.john.freezeapp.R;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.ScreenUtils;
import com.john.freezeapp.daemon.util.UIExecutor;
import com.john.freezeapp.window.FloatWindow;

public class AppMonitorService extends Service {
    private static final String ACTION_START_APP_MONITOR = "action_start_app_monitor";
    private static final String ACTION_STOP_APP_MONITOR = "action_stop_app_monitor";
    private static final String ACTION_UPDATE_TEXT_SIZE_APP_MONITOR = "action_update_text_size_app_monitor";
    AppMonitorManager.IAppMonitor iAppMonitor = this::showRunningTaskInfo;
    private static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_APP_MONITOR";
    private static final int notificationId = 1;

    @Override
    public void onCreate() {
        super.onCreate();
        if (DeviceUtil.atLeast26()) {
            createNotificationChannel(getApplicationContext());
        }

        showNotification(getApplicationContext());
    }


    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "APP监控", NotificationManager.IMPORTANCE_LOW);
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
        AppMonitorManager.unregisterAppMonitor(iAppMonitor);
        hideWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (TextUtils.equals(action, ACTION_START_APP_MONITOR)) {
            AppMonitorManager.registerAppMonitor(iAppMonitor);
            showWindow(getApplicationContext());
            return START_STICKY;
        } else if (TextUtils.equals(action, ACTION_STOP_APP_MONITOR)) {
            AppMonitorManager.unregisterAppMonitor(iAppMonitor);
            hideWindow();
            stopSelf();
            return START_NOT_STICKY;
        } else if (TextUtils.equals(action, ACTION_UPDATE_TEXT_SIZE_APP_MONITOR)) {
            updateTextViewSize();
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(Context context) {

        Notification.Builder builder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID);
        } else {
            builder = new Notification.Builder(this);
        }
        Intent intent = new Intent(context, AppMonitorActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = builder.setColor(getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentTitle("APP监控")
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
            mFloatWindow.setOnLongClickListener(v -> {
                Intent intent = new Intent(context, AppMonitorActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getApp().startActivity(intent);
                return false;
            });
            mTextView = new TextView(context);
            updateTextViewSize();
            int padding = ScreenUtils.dp2px(context, 10);
            mTextView.setPadding(padding, padding, padding, padding);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTextColor(Color.WHITE);
            mTextView.setBackgroundResource(R.drawable.mask_background);
            mTextView.setText(getShowContent(getPackageName(), AppMonitorActivity.class.getName()));
            mFloatWindow.setView(mTextView);

        }
        mFloatWindow.show();
    }

    private void updateTextViewSize() {
        if (mTextView != null) {
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AppMonitorManager.getTextSize());
        }
    }

    private void hideWindow() {
        if (mFloatWindow != null) {
            mFloatWindow.hide();
        }
    }

    private void showRunningTaskInfo(ActivityManager.RunningTaskInfo runningTaskInfo) {
        if (runningTaskInfo != null) {

            if (runningTaskInfo.topActivity != null) {
                String content = getShowContent(runningTaskInfo.topActivity.getPackageName(), runningTaskInfo.topActivity.getClassName());


                UIExecutor.postUI(new Runnable() {
                    @Override
                    public void run() {
                        if (mTextView != null) {
                            mTextView.setText(content);
                        }
                    }
                });
            }


        }
    }

    private String getShowContent(String packageName, String className) {
        return packageName +
                "\n" +
                className;
    }

    public static void startAppMonitor(Context context) {
        if (!FreezeUtil.isOverlayPermission(context)) {
            FreezeUtil.allowSystemAlertWindow();
        }
        Intent intent = new Intent(context, AppMonitorService.class);
        intent.setAction(ACTION_START_APP_MONITOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopAppMonitor(Context context) {
        Intent intent = new Intent(context, AppMonitorService.class);
        intent.setAction(ACTION_STOP_APP_MONITOR);
        context.startService(intent);
    }


    public static void updateAppMonitorTextSize(Context context) {
        Intent intent = new Intent(context, AppMonitorService.class);
        intent.setAction(ACTION_UPDATE_TEXT_SIZE_APP_MONITOR);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
