package com.john.freezeapp.freeze;

import android.widget.LinearLayout;

import com.john.freezeapp.util.FreezeAppManager;

public class FreezeAppData {
    public FreezeAppManager.AppModel appModel;
    public boolean isProcessExpand = false;
    public LinearLayout cacheView;
    public String rightName = "";
    public String right2Name = "";
    public OnItemClick onItemClick;
    public OnItemClick onItemClick2;



    public interface OnItemClick {
        void onClick(FreezeAppData data);
    }
}
