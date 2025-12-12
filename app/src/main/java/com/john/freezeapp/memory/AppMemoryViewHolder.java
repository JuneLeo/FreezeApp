package com.john.freezeapp.memory;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.freezeapp.R;
import com.john.freezeapp.appops.AppOpsData;
import com.john.freezeapp.appops.AppOpsDetailActivity;
import com.john.freezeapp.recyclerview.CardViewHolder;
import com.john.freezeapp.util.AppInfoLoader;

public class AppMemoryViewHolder extends CardViewHolder<AppMemoryModel> {

    public TextView tvName;
    public ImageView ivIcon;

    public static Creator<AppMemoryModel> CREATOR = new Creator<AppMemoryModel>() {
        @Override
        public AppMemoryViewHolder createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new AppMemoryViewHolder(inflater.inflate(R.layout.item_app_ops, parent, false));
        }
    };

    public AppMemoryViewHolder(View itemView) {
        super(itemView);
        ivIcon = itemView.findViewById(R.id.iv_image);
        tvName = itemView.findViewById(R.id.tv_name);

    }

    @Override
    public void onBind() {
        super.onBind();
        AppMemoryModel data = getData();
        AppInfoLoader.load(getContext(), data.packageName, ivIcon, tvName);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Object listener = getAdapter().getListener();
                if (listener instanceof AppMemoryAdapter.OnItemClick) {
                    ((AppMemoryAdapter.OnItemClick) listener).onItemClick(data);
                }
            }
        });
    }
}
