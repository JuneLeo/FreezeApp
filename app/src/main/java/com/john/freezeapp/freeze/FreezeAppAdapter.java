package com.john.freezeapp.freeze;

import com.john.freezeapp.recyclerview.CardRecyclerViewAdapter;
import com.john.freezeapp.recyclerview.ClassCreatorPool;
import com.john.freezeapp.util.FreezeAppManager;

import java.util.List;

public class FreezeAppAdapter extends CardRecyclerViewAdapter<ClassCreatorPool> {

    public FreezeAppAdapter() {
        getCreatorPool().putRule(FreezeAppData.class, FreezeAppViewHolder.CREATOR);
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return getItemAt(position).hashCode();
    }

    @Override
    public ClassCreatorPool onCreateCreatorPool() {
        return new ClassCreatorPool();
    }

    public void updateData(List list) {
        getItems().clear();
        getItems().addAll(list);
        notifyDataSetChanged();
    }

    public interface OnItemClick {
        void onRightClick(FreezeAppManager.AppModel appModel);
        void onRight2Click(FreezeAppManager.AppModel appModel);
    }
}
