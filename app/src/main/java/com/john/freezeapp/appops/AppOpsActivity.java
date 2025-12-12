package com.john.freezeapp.appops;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarSearchActivity;
import com.john.freezeapp.usagestats.UsageStats;
import com.john.freezeapp.usagestats.UsageStatsData;
import com.john.freezeapp.util.FreezeAppManager;
import com.john.freezeapp.util.PackageUtil;
import com.john.freezeapp.util.ThreadPool;
import com.john.freezeapp.daemon.util.UIExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppOpsActivity extends ToolbarSearchActivity {

    AppOpsAdapter mAdapter = new AppOpsAdapter(null);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_ops);

        if (!isDaemonActive()) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        requestInstallApp();
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_ops_name);
    }

    private void requestInstallApp() {
        showLoading();
        FreezeAppManager.requestAppList(this, PackageUtil.TYPE_NORMAL_APP, PackageUtil.STATUS_ALL, false, new FreezeAppManager.AppModelCallback() {
            @Override
            public void success(List<FreezeAppManager.AppModel> list) {
                hideLoading();
                requestUsageApp(list);
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

    private List<AppOpsData> mAppOpsData;

    private void updateData(List<FreezeAppManager.AppModel> userAppList, List<UsageStatsData> usageStatsDataList) {
        List<AppOpsData> data = new ArrayList<>();
        // usageStats 记录
        if (usageStatsDataList != null) {
            for (UsageStatsData usageStatsData : usageStatsDataList) {
                FreezeAppManager.AppModel app = userAppList.stream().filter(appModel -> TextUtils.equals(appModel.packageName, usageStatsData.packageName)).findFirst().orElse(null);
                if (app != null) {
                    userAppList.remove(app);
                    AppOpsData appOpsData = new AppOpsData(app);
                    data.add(appOpsData);
                }
            }
        }


        for (FreezeAppManager.AppModel appModel : userAppList) {
            data.add(new AppOpsData(appModel));
        }
        mAppOpsData = data;
        if (isDestroy()) {
            return;
        }
        updateData();
    }

    public void updateData() {
        if (mAppOpsData != null) {
            String query = getQuery();
            if (TextUtils.isEmpty(query)) {
                UIExecutor.postUI(() -> mAdapter.updateData(mAppOpsData));
            } else {
                List<AppOpsData> list = new ArrayList<>(mAppOpsData);
                ThreadPool.execute(() -> {
                    List<AppOpsData> queryAppOpsLists = new ArrayList<>();
                    for (AppOpsData appOpsData : list) {
                        FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(getContext(), appOpsData.appModel.packageName);
                        if (appModel.name != null && appModel.name.toLowerCase().contains(query.toLowerCase())) {
                            queryAppOpsLists.add(appOpsData);
                        }
                    }
                    UIExecutor.postUI(() -> mAdapter.updateData(queryAppOpsLists));
                });
            }
        }
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
}
