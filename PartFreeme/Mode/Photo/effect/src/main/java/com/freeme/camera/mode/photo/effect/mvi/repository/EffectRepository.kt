package com.freeme.camera.mode.photo.effect.mvi.repository

import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem

class EffectRepository {
    var mSelectNodes: MutableSet<ButtonItem>

    init {
        mSelectNodes = getDefaultItems()!!
    }

    fun getDefaultItems(): MutableSet<ButtonItem>? {
        return null
    }

    fun getItem(type: Int) : ButtonItem? {
        return null
    }
}