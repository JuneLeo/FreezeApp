package com.john.freezeapp.memory;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.john.freezeapp.R;
import com.john.freezeapp.ToolbarActivity;
import com.john.freezeapp.util.FreezeAppManager;

import java.util.ArrayList;
import java.util.List;

public class AppMemoryActivity extends ToolbarActivity {


    AppMemoryAdapter mAdapter = new AppMemoryAdapter(new AppMemoryAdapter.OnItemClick() {
        @Override
        public void onItemClick(AppMemoryModel model) {
            AppMemoryService.startAppMemoryMonitorFloating(AppMemoryActivity.this, model.pid, model.packageName);
        }
    });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_memory_monitor);

        if (!isDaemonActive()) {
            finish();
            return;
        }

        RecyclerView recyclerView = findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter);
        requestRunningApp();

    }


    private void requestRunningApp() {
        FreezeAppManager.requestRunningApp(this, new FreezeAppManager.RunningCallback() {
            @Override
            public void success(List<FreezeAppManager.RunningModel> list) {
                List<AppMemoryModel> models = new ArrayList<>();
                for (FreezeAppManager.RunningModel runningModel : list) {
                    for (FreezeAppManager.ProcessModel processModel : runningModel.processModels) {
                        if (TextUtils.equals(processModel.processName, runningModel.packageName)) {
                            AppMemoryModel model = new AppMemoryModel();
                            model.pid = Integer.parseInt(processModel.pid);
                            model.packageName = runningModel.packageName;
                            models.add(model);
                            break;
                        }
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.updateData(models);
                    }
                });
            }

            @Override
            public void fail() {

            }
        });
    }

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_memory_name);
    }
}
