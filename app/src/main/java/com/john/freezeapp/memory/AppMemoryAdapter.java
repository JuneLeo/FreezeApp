package com.john.freezeapp.memory;

import com.john.freezeapp.recyclerview.CardRecyclerViewAdapter;
import com.john.freezeapp.recyclerview.ClassCreatorPool;

import java.util.List;

public class AppMemoryAdapter extends CardRecyclerViewAdapter<ClassCreatorPool> {
    public AppMemoryAdapter(OnItemClick onItemClick) {
        getCreatorPool().putRule(AppMemoryModel.class, AppMemoryViewHolder.CREATOR);
        setHasStableIds(true);
        setListener(onItemClick);
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
        void onItemClick(AppMemoryModel model);
    }
}
