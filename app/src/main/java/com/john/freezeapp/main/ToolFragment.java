package com.john.freezeapp.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.john.freezeapp.BaseFragment;
import com.john.freezeapp.IDaemonBinder;
import com.john.freezeapp.R;
import com.john.freezeapp.main.tool.data.FreezeHomeToolData;
import com.john.freezeapp.main.tool.FreezeHomeToolHelper;
import com.john.freezeapp.main.tool.data.ISpanSizeLookup;
import com.john.freezeapp.recyclerview.CardData;
import com.john.freezeapp.util.FreezeUtil;
import com.john.freezeapp.util.SharedPrefUtil;

import java.util.ArrayList;
import java.util.List;

public class ToolFragment extends BaseFragment {

    FreezeMainAdapter homeAdapter = new FreezeMainAdapter();
    RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_common, container, false);
        recyclerView = view.findViewById(R.id.recycler);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                CardData toolData = homeAdapter.getItemAt(position);
                if (toolData instanceof ISpanSizeLookup) {
                    return ((ISpanSizeLookup) toolData).getSpanSize();
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(homeAdapter);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView.post(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main_tool_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        if (R.id.menu_switch_style == itemId) {
            boolean toolStyle = SharedPrefUtil.getBoolean(SharedPrefUtil.KEY_TOOL_STYLE, true);
            SharedPrefUtil.setBoolean(SharedPrefUtil.KEY_TOOL_STYLE, !toolStyle);
            updateData();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void bindDaemon(IDaemonBinder daemonBinder) {
        super.bindDaemon(daemonBinder);
        updateData();
    }

    @Override
    protected void unbindDaemon() {
        super.unbindDaemon();
        updateData();
    }

    private void updateData() {

        if (!isAdded()) {
            return;
        }

        List<CardData> list = new ArrayList<>();

        boolean toolStyle = SharedPrefUtil.getBoolean(SharedPrefUtil.KEY_TOOL_STYLE, true);
        if (toolStyle) {
            List<FreezeHomeToolData> freezeHomeFuncData = FreezeHomeToolHelper.getFreezeHomeFuncGroupData(getContext(), isDaemonActive());
            list.addAll(freezeHomeFuncData);
        } else {
            List<FreezeHomeToolData> freezeHomeFuncData = FreezeHomeToolHelper.getFreezeHomeFuncData(getContext(), isDaemonActive());
            if (freezeHomeFuncData != null) {
                list.addAll(freezeHomeFuncData);
            }
        }

        if (list.isEmpty()) {
            CommonEmptyData commonEmptyData = new CommonEmptyData();
            commonEmptyData.type = CommonEmptyData.TYPE_NOT_BIND;
            commonEmptyData.content = getContext().getString(R.string.main_home_daemon_not_active_content);
            commonEmptyData.height = recyclerView.getMeasuredHeight();
            commonEmptyData.onClickListener = v -> switchHomeFragment();
            commonEmptyData.spanSize = FreezeHomeToolData.TOOL_SINGLE;
            list.add(commonEmptyData);
        }

        homeAdapter.updateData(list);
    }

    private void switchHomeFragment() {
        FragmentActivity activity = getActivity();
        if (activity instanceof MainActivity) {
            ((MainActivity) activity).switchHomeFragment();
        }
    }


}
