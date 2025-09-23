package com.john.freezeapp.main.tool;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.display.DisplayManager;
import android.hardware.display.IDisplayManager;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.VirtualDisplay;
import android.hardware.display.VirtualDisplayConfig;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.TextureView;

import androidx.annotation.NonNull;

import com.john.freezeapp.App;
import com.john.freezeapp.BuildConfig;
import com.john.freezeapp.CommandActivity;
import com.john.freezeapp.R;
import com.john.freezeapp.appops.AppOpsActivity;
import com.john.freezeapp.battery.BatteryUsageActivity;
import com.john.freezeapp.client.ClientBinderManager;
import com.john.freezeapp.client.ClientDaemonService;
import com.john.freezeapp.client.ClientSystemService;
import com.john.freezeapp.clipboard.ClipboardActivity;
import com.john.freezeapp.daemon.DaemonHelper;
import com.john.freezeapp.deviceidle.DeviceIdleActivity;
import com.john.freezeapp.freeze.ManagerActivity;
import com.john.freezeapp.fs.FileServerActivity;
import com.john.freezeapp.main.tool.data.FreezeHomeToolData;
import com.john.freezeapp.main.tool.data.FreezeHomeToolGroupData;
import com.john.freezeapp.main.tool.data.FreezeHomeToolItemData;
import com.john.freezeapp.main.tool.data.FreezeHomeToolModel;
import com.john.freezeapp.main.tool.data.FreezeHomeToolSingleData;
import com.john.freezeapp.hyper.MiMixFlipSettingActivity;
import com.john.freezeapp.monitor.AppMonitorActivity;
import com.john.freezeapp.storage.StorageActivity;
import com.john.freezeapp.traffic.TrafficMonitorActivity;
import com.john.freezeapp.usagestats.UsageStatsActivity;
import com.john.freezeapp.usagestats.appstandby.AppStandbyActivity;
import com.john.freezeapp.util.DeviceUtil;
import com.john.freezeapp.util.PackageUtil;
import com.john.reflect.NativeFreeze;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public class FreezeHomeToolHelper {


    public static List<FreezeHomeToolData> getFreezeHomeFuncGroupData(Context context, boolean isNeedDaemonActive) {
        List<FreezeHomeToolModel> freezeHomeToolModels = getFreezeHomeToolModels(context);
        Map<Integer, List<FreezeHomeToolModel>> map = new HashMap<>();

        freezeHomeToolModels.removeIf(freezeHomeToolModel -> {
            if (freezeHomeToolModel.isNeedDaemonActive) {
                return !isNeedDaemonActive;
            } else {
                return false;
            }
        });

        for (FreezeHomeToolModel freezeHomeToolModel : freezeHomeToolModels) {
            List<FreezeHomeToolModel> modelList = map.computeIfAbsent(freezeHomeToolModel.group, k -> new ArrayList<>());
            modelList.add(freezeHomeToolModel);
        }

        List<FreezeHomeToolData> groupList = new ArrayList<>();
        for (int group : FreezeHomeToolModel.GROUPS) {
            List<FreezeHomeToolModel> modelList = map.get(group);
            if (modelList != null && !modelList.isEmpty()) {
                FreezeHomeToolGroupData freezeHomeToolGroupData = new FreezeHomeToolGroupData(group);
                freezeHomeToolGroupData.text = FreezeHomeToolModel.getGroupName(group);
                groupList.add(freezeHomeToolGroupData);
                for (FreezeHomeToolModel freezeHomeToolModel : modelList) {
                    groupList.add(FreezeHomeToolItemData.transform(freezeHomeToolModel));
                }
            }
        }
        return groupList;
    }

    public static List<FreezeHomeToolData> getFreezeHomeFuncData(Context context, boolean isNeedDaemonActive) {
        List<FreezeHomeToolModel> freezeHomeToolModels = getFreezeHomeToolModels(context);
        List<FreezeHomeToolData> singleDataList = new ArrayList<>();
        for (FreezeHomeToolModel freezeHomeToolModel : freezeHomeToolModels) {
            if (freezeHomeToolModel.isNeedDaemonActive) {
                if (isNeedDaemonActive) {
                    singleDataList.add(FreezeHomeToolSingleData.transform(freezeHomeToolModel));
                }
            } else {
                singleDataList.add(FreezeHomeToolSingleData.transform(freezeHomeToolModel));
            }

        }

        return singleDataList;
    }

    private static @NonNull List<FreezeHomeToolModel> getFreezeHomeToolModels(Context context) {
        List<FreezeHomeToolModel> list = new ArrayList<>();

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_POWER_SAVE,
                context.getResources().getString(R.string.main_manager_app),
                context.getResources().getString(R.string.main_manager_app_short_title),
                R.drawable.ic_vector_apps,
                0xffF2C8F8,
                v -> {
                    Intent intent = new Intent(context, ManagerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_USAGE,
                context.getResources().getString(R.string.main_app_usage),
                context.getResources().getString(R.string.main_app_usage_short_title),
                R.drawable.ic_vector_usage_stats,
                0xffACD4EB,
                v -> {
                    Intent intent = new Intent(context, UsageStatsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        if (DeviceUtil.atLeast31()) {
            list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_USAGE,
                    context.getResources().getString(R.string.main_battery_usage),
                    context.getResources().getString(R.string.main_battery_usage_short_title),
                    R.drawable.ic_vector_battery,
                    0xffB5EBDA,
                    v -> {
                        Intent intent = new Intent(context, BatteryUsageActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }));
        }

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_PERMISSION,
                context.getResources().getString(R.string.main_app_ops_name),
                context.getResources().getString(R.string.main_app_ops_short_title),
                R.drawable.ic_vector_key,
                0xffC1B790,
                v -> {
                    Intent intent = new Intent(context, AppOpsActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_app_monitor),
                context.getResources().getString(R.string.main_app_monitor_short_title),
                R.drawable.ic_vector_window,
                0xffEFAAB1,
                v -> {
                    Intent intent = new Intent(context, AppMonitorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        if (ClientBinderManager.isActive()) {
            if (TextUtils.equals("Xiaomi MIX Flip", ClientBinderManager.getConfig(DaemonHelper.DAEMON_MODULE_SYSTEM_PROPERTIES, "ro.product.marketname"))) {
                list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                        context.getResources().getString(R.string.main_mi_flip_setting),
                        context.getResources().getString(R.string.main_mi_flip_setting_short_title),
                        R.drawable.ic_vector_display,
                        0xff9986A4,
                        v -> {
                            Intent intent = new Intent(context, MiMixFlipSettingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                        }));
            }
        }


        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_storage_name),
                context.getResources().getString(R.string.main_storage_short_title),
                R.drawable.ic_vector_storage,
                0xffBC9DEB,
                v -> {
                    Intent intent = new Intent(context, StorageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));


        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_clipboard_name),
                context.getResources().getString(R.string.main_clipboard_short_title),
                R.drawable.ic_vector_storage,
                0xffBC9DEB,
                v -> {
                    Intent intent = new Intent(context, ClipboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        if (DeviceUtil.atLeast28()) {
            list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_POWER_SAVE,
                    context.getResources().getString(R.string.main_app_standby_bucket),
                    context.getResources().getString(R.string.main_app_standby_bucket_short_title),
                    R.drawable.ic_vector_white_terminal,
                    0xff9986A4,
                    v -> {
                        Intent intent = new Intent(context, AppStandbyActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }));
        }


        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_POWER_SAVE,
                context.getResources().getString(R.string.main_app_device_idle),
                context.getResources().getString(R.string.main_app_device_idle_short_title),
                R.drawable.ic_vector_white_terminal,
                0xff9986A4,
                v -> {
                    Intent intent = new Intent(context, DeviceIdleActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));


        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_app_file_server),
                context.getResources().getString(R.string.main_app_file_server_short_title),
                R.drawable.ic_vector_white_terminal,
                0xff9986A4,
                v -> {
                    Intent intent = new Intent(context, FileServerActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_command_app),
                context.getResources().getString(R.string.main_command_app),
                R.drawable.ic_vector_white_terminal,
                0xffF4E1B9,
                v -> {
                    Intent intent = new Intent(context, CommandActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));

        list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_TOOL,
                context.getResources().getString(R.string.main_app_traffic_monitor),
                context.getResources().getString(R.string.main_app_traffic_monitor_short_title),
                R.drawable.ic_vector_white_terminal,
                0xffF4E1B9,
                v -> {
                    Intent intent = new Intent(context, TrafficMonitorActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }));


        if (BuildConfig.DEBUG) {
            list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_OTHER,
                    context.getResources().getString(R.string.main_test),
                    context.getResources().getString(R.string.main_test),
                    R.drawable.ic_vector_window,
                    0xffD0ACA3,
                    false,
                    v -> toTest(context)));


            list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_OTHER,
                    context.getResources().getString(R.string.main_test2),
                    context.getResources().getString(R.string.main_test2),
                    R.drawable.ic_vector_window,
                    0xffD0ACA3,
                    false,
                    v -> toTest2(context)));

            list.add(new FreezeHomeToolModel(FreezeHomeToolModel.GROUP_OTHER,
                    context.getResources().getString(R.string.main_test3),
                    context.getResources().getString(R.string.main_test3),
                    R.drawable.ic_vector_window,
                    0xffD0ACA3,
                    false,
                    v -> toTest3(context)));
        }

        return list;
    }


    private static void toTest3(Context context) {
//        ClientTrafficMonitor.getHistory();
    }

    /**
     * smartspace
     *
     * @param context
     */
    private static void toTest2(Context context) {
        try {

            Class<?> activityClass = Class.forName("dalvik.system.VMRuntime");
            Method field = activityClass.getDeclaredMethod("setHiddenApiExemptions", String[].class);
            field.setAccessible(true);

            Log.i("FreezeReflect", "call success!!");
        } catch (Throwable e) {
            Log.e("FreezeReflect", "error:", e);
        }
    }


    /**
     * usagestats
     *
     * @param context
     */
    private static void toTest(Context context) {

    }
}
