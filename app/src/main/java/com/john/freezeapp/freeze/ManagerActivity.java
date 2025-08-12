package com.john.freezeapp.freeze;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarSearchActivity;
import com.john.freezeapp.util.FreezeAppManager;
import com.john.freezeapp.util.ThreadPool;
import com.john.freezeapp.daemon.util.UIExecutor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManagerActivity extends ToolbarSearchActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    List<View> viewPageViews = new ArrayList<>();
    List<String> tabs = new ArrayList<>();
    List<FreezeAppAdapter> commonAdapters = new ArrayList<>();

    FreezeAppAdapter defrostAppAdapter;
    FreezeAppAdapter freezeAppAdapter;
    FreezeAppAdapter runningAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manager);

        if (!isDaemonActive()) {
            finish();
            return;
        }

        tabs.add(getString(R.string.manager_running_app_title));
        tabs.add(getString(R.string.manager_freeze_app_title));
        tabs.add(getString(R.string.manager_defrost_app_title));

        initCommonAdapter();

        tabLayout = findViewById(R.id.tablayout);
        viewPager = findViewById(R.id.viewpage);
        viewPager.setOffscreenPageLimit(tabs.size());

        for (FreezeAppAdapter commonAdapter : commonAdapters) {
            RecyclerView recyclerView = new RecyclerView(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            recyclerView.setAdapter(commonAdapter);
            viewPageViews.add(recyclerView);
        }

        viewPager.setAdapter(new FreezeAppPageAdapter(viewPageViews, tabs));

        requestRunningApp();
        requestEnableApp();
        requestDisableApp();

        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//                closeToolbarSearch();
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_DRAGGING) {
//                    closeToolbarSearch();
                }
            }
        });

    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.manager_name);
    }

    @Override
    protected void unbindDaemon() {
        super.unbindDaemon();
        finish();
    }

    private void initCommonAdapter() {


        defrostAppAdapter = new FreezeAppAdapter();

        freezeAppAdapter = new FreezeAppAdapter();

        runningAdapter = new FreezeAppAdapter();

        commonAdapters.add(runningAdapter);
        commonAdapters.add(defrostAppAdapter);
        commonAdapters.add(freezeAppAdapter);
    }


    private void requestEnableApp() {
        showLoading();
        FreezeAppManager.requestEnableApp(this, new FreezeAppManager.Callback() {
            @Override
            public void success(List<FreezeAppManager.AppModel> list) {
                hideLoading();
                mDefrostAppLists = getFreezeApps(getResources().getString(R.string.manager_btn_freeze), list, new FreezeAppData.OnItemClick() {
                    @Override
                    public void onClick(FreezeAppData data) {
                        requestFreezeApp(data.appModel.packageName);
                    }
                });
                updateDefrostApp();
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    private void updateDefrostApp() {
        String query = getQuery();
        if (TextUtils.isEmpty(query)) {
            UIExecutor.postUI(new Runnable() {
                @Override
                public void run() {
                    defrostAppAdapter.updateData(mDefrostAppLists);
                }
            });
        } else {
            List<FreezeAppData> list = new ArrayList<>(mDefrostAppLists);
            ThreadPool.execute(() -> {
                List<FreezeAppData> queryLists = new ArrayList<>();
                for (FreezeAppData freezeAppData : list) {
                    FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(getContext(), freezeAppData.appModel.packageName);
                    if (!TextUtils.isEmpty(appModel.name) && appModel.name.toLowerCase().contains(query.toLowerCase())) {
                        queryLists.add(freezeAppData);
                    }
                }
                UIExecutor.postUI(new Runnable() {
                    @Override
                    public void run() {
                        defrostAppAdapter.updateData(queryLists);
                    }
                });
            });
        }
    }

    private void requestDisableApp() {
        showLoading();
        FreezeAppManager.requestDisableApp(this, new FreezeAppManager.Callback() {
            @Override
            public void success(List<FreezeAppManager.AppModel> list) {
                hideLoading();
                mFreezeAppLists = getFreezeApps(getResources().getString(R.string.manager_btn_defrost), list, new FreezeAppData.OnItemClick() {
                    @Override
                    public void onClick(FreezeAppData data) {
                        requestDefrostApp(data.appModel.packageName);
                    }
                });
                updateFreezeApp();
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    private void updateFreezeApp() {
        String query = getQuery();
        if (TextUtils.isEmpty(query)) {
            UIExecutor.postUI(new Runnable() {
                @Override
                public void run() {
                    freezeAppAdapter.updateData(mFreezeAppLists);
                }
            });
        } else {
            List<FreezeAppData> list = new ArrayList<>(mFreezeAppLists);
            ThreadPool.execute(() -> {
                List<FreezeAppData> queryLists = new ArrayList<>();
                for (FreezeAppData freezeAppData : list) {
                    FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(getContext(), freezeAppData.appModel.packageName);
                    if (!TextUtils.isEmpty(appModel.name) && appModel.name.toLowerCase().contains(query.toLowerCase())) {
                        queryLists.add(freezeAppData);
                    }
                }
                UIExecutor.postUI(new Runnable() {
                    @Override
                    public void run() {
                        freezeAppAdapter.updateData(queryLists);
                    }
                });
            });
        }
    }

    List<FreezeAppData> mDefrostAppLists;
    List<FreezeAppData> mFreezeAppLists;
    List<FreezeAppData> mFreezeRunningAppLists;


    private void requestFreezeApp(String packageName) {
        showLoading();
        FreezeAppManager.requestFreezeApp(packageName, new FreezeAppManager.Callback2() {
            @Override
            public void success() {
                hideLoading();
                requestEnableApp();
                requestDisableApp();
                requestRunningApp();
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    private void requestDefrostApp(String packageName) {
        showLoading();
        FreezeAppManager.requestDefrostApp(packageName, new FreezeAppManager.Callback2() {
            @Override
            public void success() {
                hideLoading();
                requestEnableApp();
                requestDisableApp();
            }

            @Override
            public void fail() {
                showLoading();
            }
        });
    }

    private void requestRunningApp() {
        showLoading();
        FreezeAppManager.requestRunningApp(this, new FreezeAppManager.Callback3() {
            @Override
            public void success(List<FreezeAppManager.RunningModel> list) {
                hideLoading();
                List<FreezeAppData> uiList = runningAdapter.getItems();
                mFreezeRunningAppLists = getFreezeRunningApp(getResources().getString(R.string.manager_btn_stop), getResources().getString(R.string.manager_btn_freeze), list, uiList,new FreezeAppData.OnItemClick() {

                    @Override
                    public void onClick(FreezeAppData data) {
                        requestForceStopApp(data.appModel.packageName);
                    }
                }, new FreezeAppData.OnItemClick() {
                    @Override
                    public void onClick(FreezeAppData data) {
                        requestFreezeApp(data.appModel.packageName);

                    }
                });
                updateFreezeRunningApp();
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    private void updateFreezeRunningApp() {
        String query = getQuery();
        if (TextUtils.isEmpty(query)) {
            UIExecutor.postUI(new Runnable() {
                @Override
                public void run() {
                    runningAdapter.updateData(mFreezeRunningAppLists);
                }
            });
        } else {
            List<FreezeAppData> list = new ArrayList<>(mFreezeRunningAppLists);
            ThreadPool.execute(() -> {
                List<FreezeAppData> queryLists = new ArrayList<>();
                for (FreezeAppData freezeAppData : list) {
                    FreezeAppManager.CacheAppModel appModel = FreezeAppManager.getAppModel(getContext(), freezeAppData.appModel.packageName);
                    if (!TextUtils.isEmpty(appModel.name) && appModel.name.toLowerCase().contains(query.toLowerCase())) {
                        queryLists.add(freezeAppData);
                    }
                }
                UIExecutor.postUI(new Runnable() {
                    @Override
                    public void run() {
                        runningAdapter.updateData(queryLists);
                    }
                });
            });
        }
    }


    private void requestForceStopApp(String packageName) {
        showLoading();
        FreezeAppManager.requestForceStopApp(packageName, new FreezeAppManager.Callback2() {
            @Override
            public void success() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoading();
                        requestRunningApp();
                    }
                });
            }

            @Override
            public void fail() {
                hideLoading();
            }
        });
    }

    public List<FreezeAppData> getFreezeApps(String operate, List<FreezeAppManager.AppModel> list, FreezeAppData.OnItemClick onClick) {
        List<FreezeAppData> freezeAppDatas = new ArrayList<>();
        for (FreezeAppManager.AppModel appModel : list) {
            FreezeAppData uiModel = new FreezeAppData();
            uiModel.appModel = appModel;
            uiModel.rightName = operate;
            uiModel.onItemClick = onClick;
            freezeAppDatas.add(uiModel);
        }
        return freezeAppDatas;
    }


    public List<FreezeAppData> getFreezeRunningApp(String operate, String operate2, List<FreezeAppManager.RunningModel> list, List<FreezeAppData> uiList, FreezeAppData.OnItemClick onClick, FreezeAppData.OnItemClick onClick2) {
        Map<String, FreezeAppData> tempMap = new HashMap<>();

        for (FreezeAppData commonUiModel : uiList) {
            tempMap.put(commonUiModel.appModel.packageName, commonUiModel);
        }
        List<FreezeAppData> newList = new ArrayList<>();
        for (FreezeAppManager.AppModel appModel : list) {
            FreezeAppData uiModel = new FreezeAppData();
            uiModel.appModel = appModel;
            uiModel.rightName = operate;
            uiModel.right2Name = operate2;
            uiModel.onItemClick = onClick;
            uiModel.onItemClick2 = onClick2;
            if (tempMap.containsKey(appModel.packageName)) {
                uiModel.isProcessExpand = tempMap.get(appModel.packageName).isProcessExpand;
            }
            newList.add(uiModel);
        }
        return newList;
    }

    @Override
    protected void onQueryTextChange(String query) {
        super.onQueryTextChange(query);
        updateFreezeApp();
        updateDefrostApp();
        updateFreezeRunningApp();
    }

    @Override
    protected void onQueryTextClose() {
        super.onQueryTextClose();
        updateFreezeApp();
        updateDefrostApp();
        updateFreezeRunningApp();
    }
}
