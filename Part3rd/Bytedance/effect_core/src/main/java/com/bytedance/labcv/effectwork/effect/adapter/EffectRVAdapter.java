package com.bytedance.labcv.effectwork.effect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.bytedance.labcv.effectwork.common.adapter.ItemViewRVAdapter;
import com.bytedance.labcv.effectwork.common.view.ButtonViewHolder;
import com.bytedance.labcv.effectwork.effect.model.EffectButtonItem;
import com.freeme.effect_core.R;

import java.util.List;

public class EffectRVAdapter extends ItemViewRVAdapter<EffectButtonItem, ButtonViewHolder>{

    private boolean mShowIndex = false;

    public EffectRVAdapter(List<EffectButtonItem> itemList, ItemViewRVAdapter.OnItemClickListener<EffectButtonItem> listener) {
        super(itemList, listener);
    }

    //  {zh} 此回调中使用定制ViewHolder的构造函数返回一个实例，inflate时可以使用不同的layout  {en} This callback uses the constructor of the custom ViewHolder to return an instance, a different layout can be used when inflating
    @Override
    public ButtonViewHolder onCreateViewHolderInternal(ViewGroup parent, int viewType) {
        return new ButtonViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.view_holder_button, parent, false));
    }

    @Override
    public void onBindViewHolderInternal(ButtonViewHolder holder, int position, EffectButtonItem item) {
        Context context = holder.itemView.getContext();

        holder.setIcon(item.getIcon());
        if (mShowIndex && position > 0){
            holder.setTitle(getIndex(position));
        }else {
            holder.setTitle(context.getString(item.getTitle()));

        }
        holder.setMarqueue(false);

        holder.change(item.shouldHighLight());

        holder.pointChange(item.shouldPointOn());
    }

    //  {zh} 需要adapter来记录item状态的情形需要实现此方法来更新adapter内的记录  {en} In situations where an adapter is required to record item status, this method needs to be implemented to update records within the adapter
    @Override
    public void changeItemSelectRecord(EffectButtonItem item, int position) {

    }

    private String getIndex(int pos){
        if (pos < 10){
            return "0"+pos;
        }else {
            return Integer.toString(pos);
        }
    }

    public void setShowIndex(boolean showIndex) {
        this.mShowIndex = showIndex;
    }

}
