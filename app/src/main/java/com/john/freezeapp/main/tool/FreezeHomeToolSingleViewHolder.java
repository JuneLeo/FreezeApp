package com.john.freezeapp.main.tool;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.john.freezeapp.R;
import com.john.freezeapp.main.tool.data.FreezeHomeToolSingleData;
import com.john.freezeapp.recyclerview.CardViewHolder;

public class FreezeHomeToolSingleViewHolder extends CardViewHolder<FreezeHomeToolSingleData> {

    TextView tvTitle;
    ImageView ivIcon;

    public static FreezeHomeToolSingleViewHolder.Creator<FreezeHomeToolSingleData> CREATOR = new FreezeHomeToolSingleViewHolder.Creator<FreezeHomeToolSingleData>() {
        @Override
        public FreezeHomeToolSingleViewHolder createViewHolder(LayoutInflater inflater, ViewGroup parent) {
            return new FreezeHomeToolSingleViewHolder(inflater.inflate(R.layout.item_home_func_single, parent, false));
        }
    };

    public FreezeHomeToolSingleViewHolder(View itemView) {
        super(itemView);
        tvTitle = itemView.findViewById(R.id.tv_title);
        ivIcon = itemView.findViewById(R.id.iv_icon);
    }

    @Override
    public void onBind() {
        super.onBind();
        FreezeHomeToolSingleData data = getData();
        tvTitle.setText(data.text);
        ivIcon.setImageResource(data.icon);
        ivIcon.setBackground(getBackgroundDrawable(data.bgColor));
        itemView.setOnClickListener(data.clickListener);
    }


    public Drawable getBackgroundDrawable(int color) {
        ShapeDrawable shapeDrawable = new ShapeDrawable(new OvalShape());
        shapeDrawable.getPaint().setColor(color);
        return shapeDrawable;
    }
}
