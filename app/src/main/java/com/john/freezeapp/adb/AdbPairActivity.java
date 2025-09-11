package com.john.freezeapp.adb;

import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.ForegroundServiceStartNotAllowedException;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.john.freezeapp.BaseActivity;
import com.john.freezeapp.R;
import com.john.freezeapp.client.ClientLog;
import com.john.freezeapp.recyclerview.CardData;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

@TargetApi(android.os.Build.VERSION_CODES.O)
public class AdbPairActivity extends BaseActivity {

    AdbPairAdapter mAdapter = new AdbPairAdapter();

    private boolean notificationEnabled = false;

    private boolean isWifi = false;


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkBroadcastReceiver);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_pair);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        notificationEnabled = isNotificationEnabled(this);
        isWifi = NetworkUtils.getConnectivityStatus(this) == NetworkUtils.NetworkType.WIFI;
        updateData();

        if (notificationEnabled && isWifi) {
            startPairingService(this);
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MANAGE_NETWORK_USAGE);
        registerReceiver(networkBroadcastReceiver, intentFilter);

    }

    private void startPairingService(Context context) {
        Intent intent = AdbPairService.startIntent(this);
        try {
            startForegroundService(intent);
        } catch (Throwable e) {
            ClientLog.error( "startForegroundService", e);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && e instanceof ForegroundServiceStartNotAllowedException) {
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(APP_OPS_SERVICE);
                int mode = appOpsManager.noteOpNoThrow("android:start_foreground", Process.myUid(), context.getPackageName(), null, null);
                if (mode == AppOpsManager.MODE_ERRORED) {
                    Toast.makeText(this, "OP_START_FOREGROUND is denied. What are you doing?", Toast.LENGTH_LONG).show();
                }
                startService(intent);
            }
        }
    }

    public BroadcastReceiver networkBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateNetwork();
        }
    };

    private void updateNetwork() {
        boolean innerIsWifi = NetworkUtils.getConnectivityStatus(this) == NetworkUtils.NetworkType.WIFI;
        if (isWifi != innerIsWifi) {
            isWifi = innerIsWifi;
            updateData();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean innerNotificationEnabled = isNotificationEnabled(this);
        boolean innerIsWifi = NetworkUtils.getConnectivityStatus(this) == NetworkUtils.NetworkType.WIFI;
        if (innerNotificationEnabled != notificationEnabled || isWifi != innerIsWifi) {
            notificationEnabled = innerNotificationEnabled;
            isWifi = innerIsWifi;
            updateData();
            if (notificationEnabled && isWifi) {
                startPairingService(this);
            }
        }
    }

    private void updateData() {
        List<CardData> list = new ArrayList<>();


        if(!isWifi) {
            AdbPairData wifiPairData = new AdbPairData();
            wifiPairData.icon = R.drawable.ic_vector_wifi;
            wifiPairData.subTitle = "配对要求您使用WIFI连接。请连接到WIFI后使用。";

            AdbPairData.AdbPairBtnData rightBtnData = new AdbPairData.AdbPairBtnData();
            rightBtnData.text = "WIFI设置";
            rightBtnData.icon = R.drawable.ic_vector_link;
            rightBtnData.show = true;
            rightBtnData.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FreezeUtil.toWifiSettingPage(AdbPairActivity.this);
                }
            };
            wifiPairData.rightBtnData = rightBtnData;

            list.add(wifiPairData);
        } else if(!notificationEnabled) {
            AdbPairData notificationPairData = new AdbPairData();
            notificationPairData.icon = R.drawable.ic_vector_error;
            notificationPairData.subTitle = "配对过程需要您与FreezeApp的通知交互。请允许FreezeApp发布通知。";

            AdbPairData.AdbPairBtnData rightBtnData = new AdbPairData.AdbPairBtnData();
            rightBtnData.text = "通知设置";
            rightBtnData.icon = R.drawable.ic_vector_link;
            rightBtnData.show = true;
            rightBtnData.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FreezeUtil.toNotificationPage(AdbPairActivity.this);
                }
            };
            notificationPairData.rightBtnData = rightBtnData;
            list.add(notificationPairData);
        } else {
            AdbPairData adbPairTipData = new AdbPairData();
            adbPairTipData.icon = R.drawable.ic_vector_check;
            adbPairTipData.subTitle = "一段来自Freeze的通知将帮助您完成配对。";
            list.add(adbPairTipData);

            AdbPairData adbPairTipData2 = new AdbPairData();
            adbPairTipData2.icon = R.drawable.ic_vector_error;
            adbPairTipData2.subTitle = "Freeze 需要访问本地网络。它由网络权限控制。\n某些系统（例如MIUI）不允许应用在不可见时访问网络，及时应用已按标准使用前台服务。请在此类系统上为Freeze 禁用电池优化功能。";
            list.add(adbPairTipData2);


            AdbPairData developPairData = new AdbPairData();
            developPairData.icon = R.drawable.ic_vector_error;
            developPairData.subTitle = "进入“开发者选项” - “无线调试”。点按“使用配对码配对设备”，您将看到一个六位数字代码。";

            AdbPairData.AdbPairBtnData rightBtnData = new AdbPairData.AdbPairBtnData();
            rightBtnData.text = "开发者选项";
            rightBtnData.icon = R.drawable.ic_vector_link;
            rightBtnData.show = true;
            rightBtnData.onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FreezeUtil.toDevelopPage(AdbPairActivity.this);
                }
            };
            developPairData.rightBtnData = rightBtnData;
            list.add(developPairData);


            if(DeviceUtil.isMIUI()) {
                AdbPairData miuiTipData = new AdbPairData();
                miuiTipData.icon = R.drawable.ic_vector_error;
                miuiTipData.subTitle = "MIUI用户可能需要在系统设置中从“通知管理”-“通知显示设置”将通知样式切换为“原生样式。\n否则，您可能会无法从通知输入配对码。”";
                list.add(miuiTipData);
            }


            AdbPairData allTipData = new AdbPairData();

            allTipData.icon = R.drawable.ic_vector_error;
            allTipData.subTitle = "1.不要关闭“开发者选项”和“USB调试”。\n2.在“开发者选项”中将“USB使用模式”改为“仅充电”。\n3.(Android 11+) 启用“停用adb授权超时功能”选项。";
            list.add(allTipData);




        }

        if (notificationEnabled) {

        } else {

        }


        mAdapter.updateData(list);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 在这里处理返回按钮的点击事件
                finish(); // 或者其他你想要执行的操作
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private boolean isNotificationEnabled(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = notificationManager.getNotificationChannel(AdbPairService.notificationChannelId);
        return notificationManager.areNotificationsEnabled() &&
                (channel == null || channel.getImportance() != NotificationManager.IMPORTANCE_NONE);
    }
}
