package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import com.freeme.camera.mode.photo.effect.model.EffectType
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem

interface IBeautyCallBack {
    fun onEffectItemClick(item: ButtonItem?)
    fun onEffectItemClose(item: ButtonItem?)
    fun getEffectType(): EffectType?
}