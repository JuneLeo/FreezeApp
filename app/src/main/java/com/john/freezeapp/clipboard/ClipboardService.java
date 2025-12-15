package com.john.freezeapp.clipboard;

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
import com.john.freezeapp.window.FloatWindow;

public class ClipboardService extends Service {
    private static final String ACTION_START_CLIPBOARD_FLOATING = "action_start_clipboard_floating";
    private static final String ACTION_STOP_CLIPBOARD_FLOATING = "action_stop_clipboard_floating";
    private static final String NOTIFICATION_CHANNEL_ID = "NOTIFICATION_CLIPBOARD_FLOATING";
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
        NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "APP粘贴板", NotificationManager.IMPORTANCE_LOW);
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
        String action = intent.getAction();
        if (TextUtils.equals(action, ACTION_START_CLIPBOARD_FLOATING)) {
            showWindow(getApplicationContext());
            return START_STICKY;
        } else if (TextUtils.equals(action, ACTION_STOP_CLIPBOARD_FLOATING)) {
            hideWindow();
            stopSelf();
            return START_NOT_STICKY;
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
        Intent intent = new Intent(context, ClipboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Notification notification = builder.setColor(getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentTitle("APP粘贴板")
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
            mFloatWindow.setOnClickListener(v -> {
                Intent intent = new Intent(context, ClipboardActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                App.getApp().startActivity(intent);
            });
            mTextView = new TextView(context);
            int padding = ScreenUtils.dp2px(context, 10);
            mTextView.setPadding(padding, padding, padding, padding);
            mTextView.setGravity(Gravity.CENTER);
            mTextView.setTextColor(Color.WHITE);
            mTextView.setBackgroundResource(R.drawable.mask_background);
            mTextView.setText("粘贴板");
            mFloatWindow.setView(mTextView);

        }
        mFloatWindow.show();
    }

    private void hideWindow() {
        if (mFloatWindow != null) {
            mFloatWindow.hide();
        }
    }

    public static void startClipboardFloating(Context context) {
        if (!FreezeUtil.isOverlayPermission(context)) {
            FreezeUtil.allowSystemAlertWindow();
        }
        Intent intent = new Intent(context, ClipboardService.class);
        intent.setAction(ACTION_START_CLIPBOARD_FLOATING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopClipboardFloating(Context context) {
        Intent intent = new Intent(context, ClipboardService.class);
        intent.setAction(ACTION_STOP_CLIPBOARD_FLOATING);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
