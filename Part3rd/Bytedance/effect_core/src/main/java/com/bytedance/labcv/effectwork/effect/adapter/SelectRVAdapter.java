package com.bytedance.labcv.effectwork.effect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.bytedance.labcv.effectwork.common.adapter.ItemViewRVAdapter;
import com.bytedance.labcv.effectwork.common.model.ButtonItem;
import com.bytedance.labcv.effectwork.common.view.SelectViewHolder;
import com.freeme.effect_core.R;

import java.util.List;

public class SelectRVAdapter<T extends ButtonItem> extends ItemViewRVAdapter<T, SelectViewHolder> {

    protected boolean mShowIndex = false;
    protected int mSelect = 0;

    public SelectRVAdapter(List<T> itemList, ItemViewRVAdapter.OnItemClickListener<T> listener) {
        super(itemList, listener);
    }

    public SelectRVAdapter(List<T> itemList, OnItemClickListener<T> listener, int selectItem) {
        super(itemList, listener);
        mSelect = selectItem;
    }


    @Override
    public SelectViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new SelectViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_select, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(SelectViewHolder holder, int position, T item) {
        Context context = holder.itemView.getContext();

        if (mSelect == position) {
            holder.change(true);
        } else {
            holder.change(false);
        }
        holder.setIcon(item.getIcon());

        if (mShowIndex && position > 0){
            holder.setTitle(getIndex(position));
        }else {
            holder.setTitle(context.getString(item.getTitle()));
        }

    }

    @Override
    public void changeItemSelectRecord(T item, int position) {
        setSelect(position);
    }

    private String getIndex(int pos){
        if (pos < 10){
            return "0"+pos;
        }else {
            return Integer.toString(pos);
        }
    }

    public void setSelect(int select) {
        if (mSelect != select) {
            int oldSelect = mSelect;
            mSelect = select;
            notifyItemChanged(oldSelect);
            notifyItemChanged(select);
        }
    }

    public T getSelectItem(){
        if (mItemList == null) {
            return null;
        }
        return mItemList.get(mSelect);
    }
}
