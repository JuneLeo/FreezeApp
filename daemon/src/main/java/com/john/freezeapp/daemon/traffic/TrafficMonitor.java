package com.john.freezeapp.daemon.traffic;

import android.app.ActivityManagerHidden;
import android.app.INotificationManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntentHidden;
import android.app.usage.NetworkStatsManagerHidden;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ParceledListSlice;
import android.graphics.Color;
import android.net.DataUsageRequest;
import android.net.INetworkStatsService;
import android.net.NetworkTemplate;
import android.net.netstats.IUsageCallback;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Base64;

import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import com.john.freezeapp.daemon.Daemon;
import com.john.freezeapp.daemon.DaemonLog;
import com.john.freezeapp.daemon.DaemonService;
import com.john.freezeapp.daemon.util.DaemonUtil;
import com.john.freezeapp.util.CommonUtil;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.ImageUtils;
import com.john.hidden.api.ReplaceRef;

import java.util.Arrays;

public class TrafficMonitor {

    private static IUsageCallbackWrapper sUsageCallbackWrapper;
    private static int sNotifyCount = 0;
    private static long sThreshold = 0;

    private static final String ICON = "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";

    public static void start(long threshold, int trafficType) {
        DaemonUtil.runShellProcess(() -> innerStart(threshold, trafficType));
    }

    private static void innerStart(long threshold, int trafficType) {
        if (sUsageCallbackWrapper != null) {
            stop();
        }

        INetworkStatsService networkStatsService = DaemonService.getNetworkStatsService();
        if (networkStatsService == null) {
            return;
        }

        sUsageCallbackWrapper = createUsageCallback();
        sThreshold = threshold;

        String callingPackage = DaemonUtil.getShellPackageName();
        if (isNewUsageCallback()) {

            NetworkTemplate networkTemplate = new NetworkTemplate.Builder(getNetworkMatchRule(trafficType)).build();
            final DataUsageRequest request = new DataUsageRequest(DataUsageRequest.REQUEST_ID_UNSET,
                    networkTemplate, threshold);
            final DataUsageRequest finalRequest = networkStatsService.registerUsageCallback(callingPackage, request, new android.net.connectivity.android.net.netstats.IUsageCallback.Stub() {
                @Override
                public void onThresholdReached(DataUsageRequest request) {
                    sUsageCallbackWrapper.onThresholdReached(request);
                }

                @Override
                public void onCallbackReleased(DataUsageRequest request) {
                    sUsageCallbackWrapper.onCallbackReleased(request);
                }
            });
            sUsageCallbackWrapper.setRequest(finalRequest);
        } else if (DeviceUtil.atLeast33()) {
            NetworkTemplate networkTemplate = new NetworkTemplate.Builder(getNetworkMatchRule(trafficType)).build();
            final DataUsageRequest request = new DataUsageRequest(DataUsageRequest.REQUEST_ID_UNSET,
                    networkTemplate, threshold);
            final DataUsageRequest finalRequest = networkStatsService.registerUsageCallback(callingPackage, request, new IUsageCallback.Stub() {
                @Override
                public void onThresholdReached(DataUsageRequest request) {
                    sUsageCallbackWrapper.onThresholdReached(request);
                }

                @Override
                public void onCallbackReleased(DataUsageRequest request) {
                    sUsageCallbackWrapper.onCallbackReleased(request);
                }
            });
            sUsageCallbackWrapper.setRequest(finalRequest);
        } else {
            NetworkTemplate networkTemplate;
            if (trafficType == TrafficConstant.TRAFFIC_MOBILE) {
                networkTemplate = NetworkTemplate.buildTemplateMobileWildcard();
            } else {
                networkTemplate = NetworkTemplate.buildTemplateWifiWildcard();
            }
            final DataUsageRequest request = new DataUsageRequest(DataUsageRequest.REQUEST_ID_UNSET,
                    networkTemplate, threshold);
            CallbackHandler callbackHandler = new CallbackHandler(Looper.getMainLooper(), networkTemplate.getMatchRule(), networkTemplate.getSubscriberId(), sUsageCallbackWrapper);
            final DataUsageRequest finalRequest = networkStatsService.registerUsageCallback(callingPackage, request, new Messenger(callbackHandler), new Binder());
            sUsageCallbackWrapper.setRequest(finalRequest);
        }


    }

    private static IUsageCallbackWrapper createUsageCallback() {
        return new IUsageCallbackWrapper() {
            DataUsageRequest request;

            @Override
            public DataUsageRequest getRequest() {
                return request;
            }

            @Override
            public void onThresholdReached(DataUsageRequest request) {
                DaemonUtil.syncRunShellProcess(() -> doTrafficMonitor(request), 500);
            }

            @Override
            public void onCallbackReleased(DataUsageRequest request) {

            }

            @Override
            public void setRequest(DataUsageRequest finalRequest) {
                this.request = finalRequest;
            }
        };
    }

    private static int getNetworkMatchRule(int trafficType) {
        if (trafficType == TrafficConstant.TRAFFIC_MOBILE) {
            return NetworkTemplate.MATCH_MOBILE;
        }
        return NetworkTemplate.MATCH_WIFI;
    }

    private static void doTrafficMonitor(DataUsageRequest request) {
        try {
            INotificationManager notificationManager = DaemonService.getNotificationManager();
            String packageName = DaemonUtil.getShellPackageName();
            if (notificationManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel notificationChannel = new NotificationChannel("trafficMonitor", "流量监控", NotificationManager.IMPORTANCE_LOW);
                    notificationChannel.setSound(null, null);
                    notificationChannel.setShowBadge(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        notificationChannel.setAllowBubbles(false);
                    }
                    notificationChannel.setImportance(NotificationManager.IMPORTANCE_HIGH);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    notificationChannel.enableVibration(true);
                    notificationChannel.setVibrationPattern(new long[]{0, 500, 300, 500});
                    notificationChannel.setBypassDnd(true); // 突破勿扰模式
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.enableLights(true);
                    notificationManager.createNotificationChannels(packageName, new ParceledListSlice(Arrays.asList(notificationChannel)));
                }

                sNotifyCount++;
                NotificationCompat.Builder builder = new NotificationCompat.Builder(Daemon.getDaemon().getContext(), "trafficMonitor");
                IconCompat withBitmap = IconCompat.createWithBitmap(ImageUtils.bytesToBitmap(Base64.decode(ICON, Base64.NO_WRAP)));;
                NotificationCompat.Builder notification = builder
                        .setContentTitle("流量监控")
                        .setContentText(String.format("%s流量使用超过阈值%s（%s次）", getNetworkText(request.template.getMatchRule()), CommonUtil.getSizeText(sThreshold), sNotifyCount))
                        .setSmallIcon(withBitmap)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setCategory(Notification.CATEGORY_CALL)
                        .setAutoCancel(false)
                        .setOngoing(true);

                if (DeviceUtil.atLeast33()) {
                    notification.setFullScreenIntent(ReplaceRef.<PendingIntent>unsafeCast(getPendingIntentActivity()), true);
                }
                notificationManager.enqueueNotificationWithTag(packageName, packageName, null, 33456, notification.build(), 0);
            }
        } catch (Exception e) {
            DaemonLog.e(e, "doTrafficMonitor");
        }
    }

    private static Object getNetworkText(int matchRule) {
        if (matchRule == NetworkTemplate.MATCH_MOBILE) {
            return "移动网络";
        }
        return "WIFI";
    }

    private static PendingIntentHidden getPendingIntentActivity() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction("com.john.freezeapp.traffic.monitor");
        IIntentSender intentSender = DaemonService.getActivityManager().getIntentSenderWithFeature(
                ActivityManagerHidden.INTENT_SENDER_ACTIVITY, DaemonUtil.getShellPackageName(),
                null, null, null, 1, new Intent[]{intent},
                null,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE, null, 0);
        return new PendingIntentHidden(intentSender);
    }


    public static void stop() {
        DaemonUtil.runShellProcess(TrafficMonitor::innerStop);
    }

    public static void innerStop() {
        INetworkStatsService networkStatsService = DaemonService.getNetworkStatsService();
        if (networkStatsService == null) {
            return;
        }

        if (sUsageCallbackWrapper != null) {
            sNotifyCount = 0;
            DataUsageRequest request = sUsageCallbackWrapper.getRequest();
            sUsageCallbackWrapper = null;
            try {
                networkStatsService.unregisterUsageRequest(request);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * fix 部分手机的IUsageCallback包名有问题
     *
     * @return
     */
    private static boolean isNewUsageCallback() {
        try {
            Class.forName("android.net.connectivity.android.net.netstats.IUsageCallback");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isActive() {
        return sUsageCallbackWrapper != null;
    }


    private interface IUsageCallbackWrapper {

        DataUsageRequest getRequest();

        void onThresholdReached(DataUsageRequest request);

        void onCallbackReleased(DataUsageRequest request);

        void setRequest(DataUsageRequest finalRequest);
    }


    private static class CallbackHandler extends Handler {
        private final int mNetworkType;
        private final String mSubscriberId;
        private IUsageCallbackWrapper mCallback;

        CallbackHandler(Looper looper, int networkType, String subscriberId,
                        IUsageCallbackWrapper callback) {
            super(looper);
            mNetworkType = networkType;
            mSubscriberId = subscriberId;
            mCallback = callback;
        }

        @Override
        public void handleMessage(Message message) {

            DataUsageRequest request =
                    (DataUsageRequest) getObject(message, DataUsageRequest.PARCELABLE_KEY);

            switch (message.what) {
                case NetworkStatsManagerHidden.CALLBACK_LIMIT_REACHED: {
                    if (mCallback != null) {
                        mCallback.onThresholdReached(request);
                    }
                    break;
                }
                case NetworkStatsManagerHidden.CALLBACK_RELEASED: {
                    if (mCallback != null) {
                        mCallback.onCallbackReleased(request);
                    }
                    break;
                }
            }
        }

        private static Object getObject(Message msg, String key) {
            return msg.getData().getParcelable(key);
        }
    }

}
