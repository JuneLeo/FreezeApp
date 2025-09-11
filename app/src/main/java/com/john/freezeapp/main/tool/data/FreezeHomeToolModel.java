package com.john.freezeapp.main.tool.data;

import android.content.Context;
import android.view.View;

public class FreezeHomeToolModel {

    public static final int GROUP_POWER_SAVE = 1;
    public static final int GROUP_PERMISSION = 2;
    public static final int GROUP_TOOL = 3;
    public static final int GROUP_USAGE = 4;
    public static final int GROUP_OTHER = 5;

    public static final int[] GROUPS = new int[]{GROUP_POWER_SAVE, GROUP_PERMISSION, GROUP_TOOL, GROUP_USAGE, GROUP_OTHER};


    public String title;
    public String shortTitle;
    public View.OnClickListener clickListener;
    public int icon;
    public int bgColor;
    public int group;
    public boolean isNeedDaemonActive = true;

    public FreezeHomeToolModel(int group, String title, String shortTitle, int icon, int bgColor, boolean isNeedDaemonActive, View.OnClickListener clickListener) {
        this.group = group;
        this.clickListener = clickListener;
        this.bgColor = bgColor;
        this.icon = icon;
        this.title = title;
        this.shortTitle = shortTitle;
        this.isNeedDaemonActive = isNeedDaemonActive;
    }

    public FreezeHomeToolModel(int group, String title, String shortTitle, int icon, int bgColor, View.OnClickListener clickListener) {
        this(group, title, shortTitle, icon, bgColor, true, clickListener);
    }

    public static String getGroupName(int group) {
        if (group == GROUP_POWER_SAVE) {
            return "超强省电";
        } else if (group == GROUP_PERMISSION) {
            return "权限管理";
        } else if (group == GROUP_TOOL) {
            return "工具";
        } else if (group == GROUP_USAGE) {
            return "使用记录";
        } else if (group == GROUP_OTHER) {
            return "其他";
        }
        return "";
    }

    public interface OnToolClick {
        void onClick(Context context);
    }


}
