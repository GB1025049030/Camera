package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem
import com.freeme.camera.mode.photo.effect.utils.CommonUtils

abstract class BaseRVAdapter<VH : RecyclerView.ViewHolder>(
    private var items: List<ButtonItem> = listOf(),
    private var listener: OnItemClickListener
) :
    RecyclerView.Adapter<VH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        return onCreateViewHolderInternal(parent, viewType)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        onBindViewHolderInternal(holder, position, item);
        holder.itemView.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                if (CommonUtils.isFastClick()) {
                    Log.e("guobin_tag", "too fast click");
                    return;
                }
                changeItemSelectRecord(item, holder.absoluteAdapterPosition);
                listener.onItemClick(item, holder.absoluteAdapterPosition);
            }
        })
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItemList(items: List<ButtonItem>) {
        this.items = items;
        notifyDataSetChanged()
    }

    abstract fun onCreateViewHolderInternal(
        parent: ViewGroup,
        viewType: Int
    ): VH

    abstract fun onBindViewHolderInternal(
        holder: VH,
        position: Int,
        item: ButtonItem
    )

    abstract fun changeItemSelectRecord(item: ButtonItem, position: Int);
}