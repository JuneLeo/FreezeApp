package com.john.freezeapp.main;

import android.view.View;

import com.john.freezeapp.main.tool.data.ISpanSizeLookup;
import com.john.freezeapp.recyclerview.CardData;

public class CommonEmptyData extends CardData implements ISpanSizeLookup {
    public static final int TYPE_NOT_BIND = 1;
    public static final int TYPE_EMPTY = 2;
    public String content;
    public int height;
    public int type;
    public View.OnClickListener onClickListener;
    public int spanSize = 1;

    @Override
    public int getSpanSize() {
        return spanSize;
    }
}
