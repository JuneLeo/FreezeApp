package com.john.freezeapp.freeze;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.john.freezeapp.R;
import com.john.freezeapp.recyclerview.CardViewHolder;
import com.john.freezeapp.util.AppInfoLoader;
import com.john.freezeapp.util.FreezeAppManager;

public class FreezeAppViewHolder extends CardViewHolder<FreezeAppData> {

    public TextView tvName;
    public ImageView ivIcon;
    public TextView tvOperate;
    public TextView tvOperate2;
    public LinearLayout llProcess;
    public ViewGroup appContainer;

    public static FreezeAppViewHolder.Creator<FreezeAppData> CREATOR = new FreezeAppViewHolder.Creator<FreezeAppData>() {
        @Override
        public FreezeAppViewHolder createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new FreezeAppViewHolder(inflater.inflate(R.layout.item_app, parent, false));
        }
    };

    public FreezeAppViewHolder(View itemView) {
        super(itemView);
        appContainer = itemView.findViewById(R.id.app_container);
        ivIcon = itemView.findViewById(R.id.iv_image);
        tvName = itemView.findViewById(R.id.tv_name);
        tvOperate = itemView.findViewById(R.id.tv_operate);
        tvOperate2 = itemView.findViewById(R.id.tv_operate2);
        llProcess = itemView.findViewById(R.id.process_info);
    }

    @Override
    public void onBind() {
        super.onBind();

        FreezeAppData data = getData();

        AppInfoLoader.load(getContext(), data.appModel.packageName, ivIcon, tvName);
        tvOperate.setText(data.rightName);
        tvOperate.setOnClickListener(v -> {
            if (data.onItemClick != null) {
                data.onItemClick.onClick(data);
            }
        });

        if (!TextUtils.isEmpty(data.right2Name)) {
            tvOperate2.setOnClickListener(v -> {
                if (data.onItemClick2 != null) {
                    data.onItemClick2.onClick(data);
                }
            });
            tvOperate2.setVisibility(View.VISIBLE);
        }

        if (data.appModel instanceof FreezeAppManager.RunningModel) {
            appContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    data.isProcessExpand = !data.isProcessExpand;
                    getAdapter().notifyDataSetChanged();
                }
            });
        }

        if (data.appModel instanceof FreezeAppManager.RunningModel) {
            if (data.cacheView == null) {
                Context context = llProcess.getContext();
                LinearLayout linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.VERTICAL);
                for (FreezeAppManager.ProcessModel processModel : ((FreezeAppManager.RunningModel) data.appModel).processModels) {
                    View processView = LayoutInflater.from(context).inflate(R.layout.process_info, null);
                    TextView processName = processView.findViewById(R.id.tv_process_name);
                    TextView processTime = processView.findViewById(R.id.tv_process_time);
                    processName.setText(processModel.processName);
                    processTime.setText("PID - " + processModel.time);
                    linearLayout.addView(processView);
                }
                data.cacheView = linearLayout;
            }

            llProcess.removeAllViews();
            ViewParent parent = data.cacheView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeAllViews();
            }
            llProcess.removeView(data.cacheView);
            llProcess.setVisibility(View.GONE);

            if (data.isProcessExpand) {
                llProcess.setVisibility(View.VISIBLE);
                llProcess.addView(data.cacheView);
            }
        }
    }
}
