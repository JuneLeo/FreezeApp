package com.john.freezeapp.main;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.john.freezeapp.IDaemonBinder;
import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarActivity;
import com.john.freezeapp.client.ClientLog;
import com.john.freezeapp.util.FreezeAppManager;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.PackageUtil;
import com.john.freezeapp.util.SharedPrefUtil;
import com.john.freezeapp.util.ThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;


public class MainActivity extends ToolbarActivity {

    ViewPager viewPager;

    private final List<Tab> tabs = new ArrayList<>();

    public static class Tab {
        public int itemId;
        public int title;
        public Fragment fragment;

        public Tab(int itemId, int title, Fragment fragment) {
            this.fragment = fragment;
            this.itemId = itemId;
            this.title = title;
        }
    }

    @Override
    protected void initToolbar(Toolbar toolbar) {
        setSupportActionBar(toolbar);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FreezeUtil.generateShell(this);
        tabs.add(new Tab(R.id.navigation_home, R.string.app_name, new HomeFragment()));
        tabs.add(new Tab(R.id.navigation_tool, R.string.main_home_func, new ToolFragment()));
        tabs.add(new Tab(R.id.navigation_log, R.string.main_home_log, new LogFragment()));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setOffscreenPageLimit(tabs.size());
        viewPager.setAdapter(new MainAdapter(tabs, getSupportFragmentManager()));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                getSupportActionBar().setTitle(tabs.get(position).title);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                for (int i = 0; i < tabs.size(); i++) {
                    if (tabs.get(i).itemId == item.getItemId()) {
                        viewPager.setCurrentItem(i);
                        return true;
                    }
                }
                return false;
            }
        });
        initPackage();

    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_name);
    }

    public static class MainAdapter extends FragmentPagerAdapter {
        List<Tab> fragments;

        public MainAdapter(List<Tab> tabs, @NonNull FragmentManager fm) {
            super(fm);
            this.fragments = tabs;
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragments.get(position).fragment;
        }

        @Override
        public int getCount() {
            return fragments.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void bindDaemon(IDaemonBinder daemonBinder) {
        super.bindDaemon(daemonBinder);
        initPackage();
        if (SharedPrefUtil.isFirstBindDaemon()) {
            postUI(new Runnable() {
                @Override
                public void run() {
                    switchToolFragment();
                }
            });
            SharedPrefUtil.setFirstBindDaemon();
        }

    }

    private void initPackage() {
        if (!isDaemonActive()) {
            return;
        }
        FreezeAppManager.requestAppList(getContext(), PackageUtil.TYPE_ALL, PackageUtil.STATUS_ALL, true, new FreezeAppManager.AppModelCallback() {
            @Override
            public void success(List<FreezeAppManager.AppModel> list) {
                ExecutorService executorService = ThreadPool.createExecutorService(4);
                ClientLog.log("initPackage - start");
                List<List<FreezeAppManager.AppModel>> lists = FreezeUtil.averageAssign(list, 4);
                for (List<FreezeAppManager.AppModel> appModels : lists) {
                    executorService.execute(() -> {
                        for (FreezeAppManager.AppModel appModel : appModels) {
                            if (!TextUtils.isEmpty(appModel.packageName)) {
                                FreezeAppManager.getAppModel(getContext(), appModel.packageName);
                            }
                        }
                        ClientLog.log("initPackage - done");
                    });
                }


                executorService.shutdown();

            }

            @Override
            public void fail() {

            }
        });
    }

    @Override
    protected void unbindDaemon() {
        super.unbindDaemon();
//        if (SharedPrefUtil.isFirstUnbindDaemon()) {
//            SharedPrefUtil.setFirstUnbindDaemon();
//        }
        switchHomeFragment();
    }

    public void switchHomeFragment() {
        switchFragment(R.id.navigation_home);
    }

    public void switchToolFragment() {
        switchFragment(R.id.navigation_tool);
    }

    private void switchFragment(int menu) {
        postUI(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < tabs.size(); i++) {
                    Tab tab = tabs.get(i);
                    if (tab.itemId == menu) {
                        viewPager.setCurrentItem(i);
                    }
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        ClientLog.log("");
    }
}
