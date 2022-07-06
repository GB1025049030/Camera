package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem

interface OnItemClickListener {
    fun onItemClick(item: ButtonItem, position: Int)
}