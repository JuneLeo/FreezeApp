package com.john.freezeapp.autoglm;

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

import com.john.freezeapp.R;
import com.john.freezeapp.clipboard.ClipboardActivity;
import com.john.freezeapp.daemon.autoglm.IAutoGLMBinder;
import com.john.freezeapp.daemon.autoglm.IAutoGLMListener;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.ScreenUtils;
import com.john.freezeapp.window.FloatWindow;

public class AutoGLMService extends Service {
    private static final String ACTION_START_AUTO_GLM_MONITOR = "action_start_auto_glm_monitor";
    private static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_AUTO_GLM_MONITOR_FLOATING";
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
            case ACTION_START_AUTO_GLM_MONITOR: {
                startAutoGLMMonitor();
                showWindow(getApplicationContext());
            }
            return START_STICKY;
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void updateText(String text) {
        if (mTextView != null) {
            mTextView.post(new Runnable() {
                @Override
                public void run() {
                    mTextView.setText(text);
                }
            });
        }
    }

    private void startAutoGLMMonitor() {
        IAutoGLMBinder autoGLMBinder = AutoGLMManager.getAutoGLMBinder();
        if (autoGLMBinder != null) {
            try {
                autoGLMBinder.addListener(new IAutoGLMListener.Stub() {
                    @Override
                    public void start() throws RemoteException {
                        updateText("start");
                    }

                    @Override
                    public void end() throws RemoteException {
                        updateText("end");
                    }

                    @Override
                    public void process(String action) throws RemoteException {
                        updateText(action);
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
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

    private void hideWindow() {
        if (mFloatWindow != null) {
            mFloatWindow.hide();
        }
    }

    public static void start(Context context) {

        if (!FreezeUtil.isOverlayPermission(context)) {
            FreezeUtil.allowSystemAlertWindow();
        }

        Intent intent = new Intent(context, AutoGLMService.class);
        intent.setAction(ACTION_START_AUTO_GLM_MONITOR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
