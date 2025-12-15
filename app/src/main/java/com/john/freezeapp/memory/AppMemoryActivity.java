package com.john.freezeapp.memory;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_memory_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.close_monitor) {
            AppMemoryService.stopAppMemoryMonitorFloating(getContext());
            return true;
        }
        return super.onOptionsItemSelected(item);
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
