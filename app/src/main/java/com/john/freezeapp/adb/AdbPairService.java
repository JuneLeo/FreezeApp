package com.john.freezeapp.adb;

import android.annotation.TargetApi;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RemoteInput;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;

import com.john.adb.AdbInvalidPairingCodeException;
import com.john.adb.AdbKey;
import com.john.adb.AdbKeyException;
import com.john.adb.AdbMdns;
import com.john.adb.AdbPairingClient;
import com.john.freezeapp.R;
import com.john.freezeapp.client.ClientLog;
import com.john.freezeapp.util.SharedPrefUtil;
import com.john.freezeapp.util.ThreadPool;

import java.net.ConnectException;

@TargetApi(Build.VERSION_CODES.O)
public class AdbPairService extends Service {

    public static final String notificationChannelId = "adb_pairing";

    private static final int notificationId = 1;
    private static final int replyRequestId = 1;
    private static final int stopRequestId = 2;
    private static final int retryRequestId = 3;
    private static final String startAction = "start";
    private static final String stopAction = "stop";
    private static final String replyAction = "reply";
    private static final String remoteInputResultKey = "paring_code";
    private static final String portKey = "paring_code";

    private Handler handler = new Handler(Looper.getMainLooper());
    private int port;
    private AdbMdns adbMdns = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * getSystemService(NotificationManager::class.java).createNotificationChannel(
     * NotificationChannel(
     * notificationChannel,
     * getString(R.string.notification_channel_adb_pairing),
     * NotificationManager.IMPORTANCE_HIGH
     * ).apply {
     * setSound(null, null)
     * setShowBadge(false)
     * setAllowBubbles(false)
     * })
     */

    @Override
    public void onCreate() {
        super.onCreate();
        createSearchNotification();
    }

    private void createSearchNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, getString(R.string.notification_channel_adb_pairing), NotificationManager.IMPORTANCE_HIGH);
        notificationChannel.setSound(null, null);
        notificationChannel.setShowBadge(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            notificationChannel.setAllowBubbles(false);
        }
        notificationManager.createNotificationChannel(notificationChannel);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        switch (action) {
            case startAction:
                startAction(intent);
                break;
            case stopAction:
                stopForeground(STOP_FOREGROUND_REMOVE);
                break;
            case replyAction:
                Bundle resultsFromIntent = RemoteInput.getResultsFromIntent(intent);
                String code = "";
                if (resultsFromIntent != null) {
                    code = (String) resultsFromIntent.getCharSequence(remoteInputResultKey);
                }
                int port = intent.getIntExtra(portKey, -1);
                if (port != -1) {
                    onInput(code, port);
                } else {
                    startAction(intent);
                }

                break;
            default:
                return START_NOT_STICKY;
        }

        return START_REDELIVER_INTENT;

    }

    private void onInput(String code, final int port) {
        ThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                String host = "127.0.0.1";

                AdbKey key = null;
                try {
                    key = new AdbKey(new AdbKey.PreferenceAdbKeyStore(SharedPrefUtil.getSharedPref()), "freezeapp");
                } catch (Throwable e){
                    return;
                }
                AdbPairingClient client =  new AdbPairingClient(host, key, code, port);
                try {
                    boolean result = client.start();
                    handleResult(result, null);
                } catch (Exception e) {
                   e.printStackTrace();
                   handleResult(false, e);
                }
            }
        });

        startNotification(workingNotification());
    }

    public void startAction(Intent intent) {
        startSearch();
        startNotification(createSearchingNotification());
    }


    public void startNotification(Notification notification) {
        if (notification != null) {
            try {
                startForeground(notificationId, notification);
            } catch (Throwable e) {
               ClientLog.error("startForeground failed", e);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && e instanceof ForegroundServiceStartNotAllowedException) {
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.notify(notificationId, notification);
                }
            }
        }
    }


    private void handleResult(boolean success, Throwable exception) {
        stopForeground(STOP_FOREGROUND_REMOVE);

        String title = null;
        String text = null;

        if (success) {
            title = getString(R.string.notification_adb_pairing_succeed_title);
            text = getString(R.string.notification_adb_pairing_succeed_text);
            stopSearch();
        } else {
            title = getString(R.string.notification_adb_pairing_failed_title);

            if (exception instanceof ConnectException) {
                text = getString(R.string.cannot_connect_port);
            } else if (exception instanceof AdbInvalidPairingCodeException) {
                text = getString(R.string.paring_code_is_wrong);
            } else if (exception instanceof AdbKeyException) {
                text = getString(R.string.adb_error_key_store);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification.Builder(this, notificationChannelId)
                .setColor(getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentTitle(title)
                .setContentText(text)
                .build();
        notificationManager.notify(notificationId, notification);
    }

    private void stopSearch() {
        if (!started) return;
        started = false;
        if (adbMdns != null) {
            adbMdns.stop();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSearch();
    }

    private boolean started = false;

    private void startSearch() {
        if (started) return;
        started = true;
        adbMdns = new AdbMdns(this, port, AdbMdns.TLS_PAIRING, new AdbMdns.Callback() {
            @Override
            public void callback(int port) {
                AdbPairService.this.port = port;
                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        ClientLog.i( "Pairing service port: $port");

                        // Since the service could be killed before user finishing input,
                        // we need to put the port into Intent
                        Notification notification = createInputNotification(port);
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(notificationId, notification);
                    }
                });
            }
        });
        adbMdns.start();
    }


    private Notification createSearchingNotification() {
        return new Notification.Builder(this, notificationChannelId)
                .setColor(getColor(R.color.colorAccent))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .setContentTitle(getString(R.string.notification_adb_pairing_searching_for_service_title))
                .addAction(stopNotificationAction())
                .build();
    }

    private Notification createInputNotification(int port) {
        return new Notification.Builder(this, notificationChannelId)
                .setColor(getColor(R.color.colorAccent))
                .setContentTitle(getString(R.string.notification_adb_pairing_service_found_title))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .addAction(replyNotificationAction(port))
                .build();
    }

    private Notification workingNotification() {
        return new Notification.Builder(this, notificationChannelId)
                .setColor(getColor(R.color.colorAccent))
                .setContentTitle(getString(R.string.notification_adb_pairing_working_title))
                .setSmallIcon(R.mipmap.ic_app_icon)
                .build();
    }


    public static Intent startIntent(Context context) {
        Intent intent = new Intent(context, AdbPairService.class);
        intent.setAction(startAction);
        return intent;
    }

    private Intent stopIntent(Context context) {
        Intent intent = new Intent(context, AdbPairService.class);
        intent.setAction(stopAction);
        return intent;
    }

    private Intent replyIntent(Context context, int port) {
        Intent intent = new Intent(context, AdbPairService.class);
        intent.setAction(replyAction);
        intent.putExtra(portKey, port);
        return intent;
    }

    private Notification.Action stopNotificationAction() {
        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                stopRequestId,
                stopIntent(this), Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0);
        return new Notification.Action.Builder(null, getString(R.string.notification_adb_pairing_stop_searching), pendingIntent).build();
    }


    private Notification.Action retryNotificationAction() {

        PendingIntent pendingIntent = PendingIntent.getService(
                this,
                retryRequestId,
                stopIntent(this), Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0);
        return new Notification.Action.Builder(null, getString(R.string.notification_adb_pairing_retry), pendingIntent).build();
    }

    private Notification.Action replyNotificationAction() {

        RemoteInput remoteInput = new RemoteInput.Builder(remoteInputResultKey)
                .setLabel(getString(R.string.dialog_adb_pairing_paring_code))
                .build();

        PendingIntent pendingIntent = PendingIntent.getForegroundService(
                this,
                replyRequestId,
                replyIntent(this, -1),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT);

        return new Notification.Action.Builder(null, getString(R.string.notification_adb_pairing_input_paring_code), pendingIntent)
                .addRemoteInput(remoteInput)
                .build();
    }

    private Notification.Action replyNotificationAction(int port) {

        Notification.Action action = replyNotificationAction();

        action.actionIntent = PendingIntent.getForegroundService(
                this,
                replyRequestId,
                replyIntent(this, port),
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT

        );

        return action;
    }
}
