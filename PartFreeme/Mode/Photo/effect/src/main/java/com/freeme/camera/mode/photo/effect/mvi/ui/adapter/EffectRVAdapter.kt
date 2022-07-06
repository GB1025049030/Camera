package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.freeme.camera.mode.photo.effect.databinding.ViewHolderButtonBinding
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem

class EffectRVAdapter @JvmOverloads constructor(
    listener: OnItemClickListener,
    items: List<ButtonItem> = listOf()
) : BaseRVAdapter<ButtonViewHolder>(items, listener) {

    private lateinit var binding: ViewHolderButtonBinding
    var mShowIndex = false

    override fun onCreateViewHolderInternal(
        parent: ViewGroup,
        viewType: Int
    ): ButtonViewHolder {
        binding = ViewHolderButtonBinding.inflate(LayoutInflater.from(parent.context))
        return ButtonViewHolder(binding)
    }

    override fun onBindViewHolderInternal(
        holder: ButtonViewHolder,
        position: Int,
        item: ButtonItem
    ) {
        val context = holder.itemView.context

        holder.setIcon(item.icon)
        if (mShowIndex && position > 0) {
            holder.setTitle(getIndex(position))
        } else {
            holder.setTitle(context.getString(item.title))
        }
        holder.setMarquee(false)
        holder.change(item.shouldHighLight())
        holder.pointChange(item.shouldPointOn())
    }

    private fun getIndex(pos: Int): String {
        return if (pos < 10) {
            "0$pos"
        } else {
            pos.toString()
        }
    }

    override fun changeItemSelectRecord(item: ButtonItem, position: Int) {
        TODO("Not yet implemented")
    }
}