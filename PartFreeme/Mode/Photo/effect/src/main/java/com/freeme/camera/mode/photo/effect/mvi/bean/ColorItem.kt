package com.freeme.camera.mode.photo.effect.mvi.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ColorItem @JvmOverloads constructor(
    var title: Int,
    var r: Float,
    var g: Float,
    var b: Float,
    var a: Float = 1f
) : Parcelable {
    fun toInt(): Int {
        return (a * 255).toInt() shl 24 or ((r * 255).toInt() shl 16) or ((g * 255).toInt() shl 8) or (b * 255).toInt()
    }
}