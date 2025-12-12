package com.john.freezeapp.hyper;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aigestudio.wheelpicker.WheelPicker;
import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarSearchActivity;
import com.john.freezeapp.recyclerview.CardData;
import com.john.freezeapp.usagestats.UsageStats;
import com.john.freezeapp.usagestats.UsageStatsData;
import com.john.freezeapp.util.FreezeAppManager;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.ThreadPool;
import com.john.freezeapp.daemon.util.UIExecutor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MiMixFlipSettingActivity extends ToolbarSearchActivity {

    public static String[] scaleValues = {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9", "1.0"};

    MiMixFlipSettingAdapter mAdapter = new MiMixFlipSettingAdapter(new MiMixFlipSettingAdapter.OnItemClickListener() {
        @Override
        public void forceStop(CardData data) {
            if (data instanceof MiMixFlipAppData) {
                forceStopOtherApp(MiMixFlipSettingActivity.this, (MiMixFlipAppData) data);
            }
        }

        @Override
        public void scaleSetting(CardData data) {
            if (data instanceof MiMixFlipAppData) {
                showScaleDialog(MiMixFlipSettingActivity.this, (MiMixFlipAppData) data);
            }

        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mi_flip_setting);

        if (!isDaemonActive()) {
            finish();
            return;
        }

        TextView tvTip = findViewById(R.id.tip);
        tvTip.setText(getTipString(this));
        tvTip.setMovementMethod(LinkMovementMethod.getInstance());
        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        requestAllUserApp();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.mi_flip_setting);
    }

    private void forceStopOtherApp(Context context, MiMixFlipAppData data) {
        String packageName = data.appModel.packageName;
        if (FreezeUtil.isFreezeApp(packageName)) {
            Toast.makeText(MiMixFlipSettingActivity.this, "请手动强杀" + context.getString(R.string.app_name) + "~", Toast.LENGTH_SHORT).show();
            return;
        }
        FreezeAppManager.requestForceStopApp(packageName, new FreezeAppManager.ActionCallback() {
            @Override
            public void success() {
                if (isDestroy()) {
                    return;
                }

                postUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, getAppName(context, packageName) + "应用强杀成功~", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void fail() {

            }
        });
    }

    private String getAppName(Context context, String packageName) {
        String name = "";
        FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(context, packageName);
        if (!TextUtils.isEmpty(appModel.name)) {
            name = appModel.name;
        }
        return name;

    }

    private void updateScaleSetting(Context context, String packageName, String scaleValue) {
        MixFlipUtil.allowStartApps(packageName);
        MixFlipUtil.configAppScale(packageName, scaleValue);
        if (!FreezeUtil.isFreezeApp(packageName)) {
            postDelayUI(new Runnable() {
                @Override
                public void run() {
                    FreezeAppManager.requestForceStopApp(packageName, null);
                    if (isDestroy()) {
                        return;
                    }
                    Toast.makeText(context, getAppName(context, packageName) + "缩放配置修改成功～", Toast.LENGTH_SHORT).show();

                }
            }, 100);
        }
    }

    private void showScaleDialog(Context context, MiMixFlipAppData data) {
        WheelPicker wheelPicker = new WheelPicker(context);
        wheelPicker.setSelectedItemTextColor(context.getColor(R.color.colorAccent));
        List<String> list = Arrays.asList(scaleValues);
        wheelPicker.setData(list);
        int index = list.indexOf(data.scale);
        if (index != -1) {
            wheelPicker.setSelectedItemPosition(index, false);
        }
        String packageName = data.appModel.packageName;
        new AlertDialog.Builder(context)
                .setView(wheelPicker)
                .setPositiveButton(R.string.btn_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String scaleValue = scaleValues[wheelPicker.getCurrentItemPosition()];
                        MiMixFlipStorage.setScale(packageName, scaleValue);
                        data.scale = scaleValue;
                        mAdapter.notifyDataSetChanged();
                        updateScaleSetting(MiMixFlipSettingActivity.this, packageName, scaleValue);
                    }
                })
                .setNegativeButton(R.string.btn_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    private void requestAllUserApp() {
        showLoading();
        FreezeAppManager.requestAllUserApp(this, new FreezeAppManager.AppModelCallback() {
            @Override
            public void success(List<FreezeAppManager.AppModel> list) {
                requestUsageApp(list);
                hideLoading();
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    private void requestUsageApp(List<FreezeAppManager.AppModel> list) {
        showLoading();
        UsageStats.requestUsageStatsData(this, new UsageStats.Callback() {
            @Override
            public void success(List<UsageStatsData> usageStatsDataList) {
                usageStatsDataList.removeIf(next -> next.totalTimeVisible <= 0);
                Collections.sort(usageStatsDataList);
                updateData(list, usageStatsDataList);
                hideLoading();
            }

            @Override
            public void fail() {
                updateData(list, null);
                hideLoading();
            }
        });
    }

    private void updateData(List<FreezeAppManager.AppModel> userAppList, List<UsageStatsData> usageStatsDataList) {
        List<MiMixFlipAppData> data = new ArrayList<>();
        Map<String, String> scaleMap = MiMixFlipStorage.getScale();


        // 本地有Scale记录
        if (scaleMap != null) {
            for (Map.Entry<String, String> entry : scaleMap.entrySet()) {
                FreezeAppManager.AppModel app = userAppList.stream().filter(appModel -> TextUtils.equals(appModel.packageName, entry.getKey())).findFirst().orElse(null);
                if (app != null) {
                    userAppList.remove(app);
                    MiMixFlipAppData miMixFlipAppData = new MiMixFlipAppData();
                    miMixFlipAppData.scale = entry.getValue();
                    miMixFlipAppData.appModel = app;
                    if (FreezeUtil.isFreezeApp(entry.getKey())) {
                        data.add(0, miMixFlipAppData);
                    } else {
                        data.add(miMixFlipAppData);
                    }
                }
            }
        }

        // usageStats 记录
        if (usageStatsDataList != null) {
            for (UsageStatsData usageStatsData : usageStatsDataList) {
                FreezeAppManager.AppModel app = userAppList.stream().filter(appModel -> TextUtils.equals(appModel.packageName, usageStatsData.packageName)).findFirst().orElse(null);
                if (app != null) {
                    userAppList.remove(app);
                    MiMixFlipAppData miMixFlipAppData = new MiMixFlipAppData();
                    miMixFlipAppData.appModel = app;
                    if (FreezeUtil.isFreezeApp(usageStatsData.packageName)) {
                        data.add(0, miMixFlipAppData);
                    } else {
                        data.add(miMixFlipAppData);
                    }
                }
            }
        }


        for (FreezeAppManager.AppModel appModel : userAppList) {
            MiMixFlipAppData miMixFlipAppData = new MiMixFlipAppData();
            miMixFlipAppData.appModel = appModel;
            if (FreezeUtil.isFreezeApp(appModel.packageName)) {
                data.add(0, miMixFlipAppData);
            } else {
                data.add(miMixFlipAppData);
            }
        }

        if (isDestroy()) {
            return;
        }
        mMiMixFlipAppData = data;
        updateData();
    }

    List<MiMixFlipAppData> mMiMixFlipAppData;


    private void allowAllApp(Context context) {
        List<PackageInfo> installUserApp = FreezeAppManager.getInstallUserApp();
        List<String> packages = installUserApp.stream().map(packageInfo -> packageInfo.packageName).collect(Collectors.toList());
        MixFlipUtil.allowStartApps(packages);
        postDelayUI(new Runnable() {
            @Override
            public void run() {
                forceStopFlipApp(MiMixFlipSettingActivity.this);
                if (isDestroy()) {
                    return;
                }
                Toast.makeText(context, "配置成功", Toast.LENGTH_SHORT).show();
            }
        }, 100);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_mi_mix_flip_menu, menu);
        initSearchMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        if (R.id.menu_flip_open == itemId) {
            openFlipHome();
            return true;
        } else if (R.id.menu_flip_reset_scale == itemId) {
            resetScaleConig();
            return true;
        } else if (R.id.mi_mix_flip_home_allow_start == itemId) {
            allowAllApp(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetScaleConig() {
        MixFlipUtil.resetConfigAppScale();
        MiMixFlipStorage.resetScale();
        List<Object> items = mAdapter.getItems();
        if (items != null && !items.isEmpty()) {
            for (Object item : items) {
                if (item instanceof MiMixFlipAppData) {
                    ((MiMixFlipAppData) item).scale = null;
                    mAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void openFlipHome() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClassName("com.miui.fliphome", "com.miui.fliphome.settings.AppSettingActivity");
        startActivity(intent);
    }

    private void forceStopFlipApp(Context context) {
        FreezeAppManager.requestForceStopApp("com.miui.fliphome", new FreezeAppManager.ActionCallback() {
            @Override
            public void success() {
                postUI(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "外屏应用强杀成功～", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void fail() {

            }
        });
    }

    private SpannableString getTipString(Context context) {
        String configString = context.getString(R.string.mi_mix_flip_home_allow_start);

        String appString = context.getString(R.string.mi_mix_flip_home_app);
        String allString = context.getString(R.string.mi_mix_flip_tip, configString, appString);
        SpannableString spannable = new SpannableString(allString);

        int start = allString.indexOf(appString);
        int end = start + appString.length();

        spannable.setSpan(new ForegroundColorSpan(context.getColor(R.color.colorAccent)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                openFlipHome();
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

        start = allString.indexOf(configString);
        end = start + configString.length();

        spannable.setSpan(new ForegroundColorSpan(context.getColor(R.color.colorAccent)), start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                allowAllApp(MiMixFlipSettingActivity.this);
            }
        }, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);


        return spannable;

    }


    @Override
    protected void onQueryTextChange(String query) {
        super.onQueryTextChange(query);
        updateData();
    }

    @Override
    protected void onQueryTextClose() {
        super.onQueryTextClose();
        updateData();
    }

    private void updateData() {
        if (mMiMixFlipAppData != null) {
            String query = getQuery();
            if (TextUtils.isEmpty(query)) {
                UIExecutor.postUI(() -> mAdapter.updateData(mMiMixFlipAppData));
            } else {
                List<MiMixFlipAppData> list = new ArrayList<>(mMiMixFlipAppData);
                ThreadPool.execute(() -> {
                    List<MiMixFlipAppData> queryLists = new ArrayList<>();
                    for (MiMixFlipAppData miMixFlipAppData : list) {
                        FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(getContext(), miMixFlipAppData.appModel.packageName);
                        if (!TextUtils.isEmpty(appModel.name) && appModel.name.toLowerCase().contains(query.toLowerCase())) {
                            queryLists.add(miMixFlipAppData);
                        }
                    }
                    UIExecutor.postUI(() -> mAdapter.updateData(queryLists));
                });
            }
        }
    }

}
