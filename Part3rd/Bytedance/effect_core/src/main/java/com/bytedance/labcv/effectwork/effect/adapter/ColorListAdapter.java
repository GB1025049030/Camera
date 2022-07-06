package com.bytedance.labcv.effectwork.effect.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bytedance.labcv.core.util.LogUtils;
import com.bytedance.labcv.effectwork.common.utils.CommonUtils;
import com.bytedance.labcv.effectwork.effect.model.ColorItem;
import com.bytedance.labcv.effectwork.effect.view.ColorCircleView;
import com.freeme.effect_core.R;

import java.util.List;

public  class ColorListAdapter extends  RecyclerView.Adapter<ColorListAdapter.ViewHolder>{
    protected   List<ColorItem> mItemList;

    protected   OnItemClickListener mListener;


    protected int mSelect = 0;

    public void setSelect(int select) {
        if (mSelect != select) {
            int oldSelect = mSelect;
            mSelect = select;
            notifyItemChanged(oldSelect);
            notifyItemChanged(select);
        }
    }

    public int getSelect() {
        return mSelect;
    }
    public ColorListAdapter(Context mContext) {
    }

    public ColorListAdapter(Context context, List list, OnItemClickListener listener) {
    }

    public void setData( List<ColorItem> list, OnItemClickListener listener){
        mItemList = list;
        mListener = listener;
        notifyDataSetChanged();
    }







    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_color_select, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final ColorItem item = mItemList.get(position);

        onBindViewHolderInternal(holder, mSelect == position, item);

        holder.itemView.setOnClickListener(v -> {
            if (CommonUtils.isFastClick()) {
                LogUtils.e("too fast click");
                return;
            }
            setSelect(position);
            mListener.onItemClick(item, position);
        });
    }

    @Override
    public int getItemCount() {
        return mItemList==null?0:mItemList.size();    }

    protected void onBindViewHolderInternal(ViewHolder holder, boolean select, ColorItem color) {

        holder.colorCircleView.setSelected(select);
        holder.colorCircleView.setmInnerColor(color);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout ll;
        ColorCircleView colorCircleView;

        ViewHolder(View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.ll_item);
            colorCircleView = itemView.findViewById(R.id.iv_item);
        }
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item, int position);
    }

}
