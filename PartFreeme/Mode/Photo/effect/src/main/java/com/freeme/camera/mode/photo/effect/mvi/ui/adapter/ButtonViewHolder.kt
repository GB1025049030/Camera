package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.freeme.camera.mode.photo.effect.R
import com.freeme.camera.mode.photo.effect.databinding.ViewHolderButtonBinding
import com.freeme.camera.mode.photo.effect.view.MarqueeTextView

class ButtonViewHolder(private val binding: ViewHolderButtonBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private var colorOn = 0
    private var colorOff = 0
    private var isOn = false
    private var isPointOn = false

    init {
        colorOn = ActivityCompat.getColor(itemView.context, R.color.colorWhite)
        colorOff = ActivityCompat.getColor(itemView.context, R.color.colorGrey)
    }

    fun setIcon(iconResource: Int) {
        binding.ivFaceOptions.setImageResource(iconResource)
    }

    fun setTitle(title: String) {
        if (title.isEmpty()) {
            binding.tvTitleFaceOptions.visibility = View.GONE
        } else {
            binding.tvTitleFaceOptions.visibility = View.VISIBLE
            binding.tvTitleFaceOptions.text = title
        }
    }

    fun setDesc(desc: String?) {
        if (desc == null || desc.isEmpty()) {
            binding.tvDescFaceOptions.visibility = View.GONE
        } else {
            binding.tvDescFaceOptions.visibility = View.VISIBLE
            binding.tvDescFaceOptions.text = desc
        }
    }

    fun change(on: Boolean) {
        if (on) {
            on()
        } else {
            off()
        }
    }

    private fun on() {
        isOn = true
        setColor(colorOn)
    }

    private fun off() {
        isOn = false
        setColor(colorOff)
    }

    private fun setColor(color: Int) {
        val drawable: Drawable = binding.ivFaceOptions.drawable ?: return
        DrawableCompat.setTint(drawable, color)
        binding.ivFaceOptions.setImageDrawable(drawable)
        binding.tvTitleFaceOptions.setTextColor(color)
        binding.tvDescFaceOptions.setTextColor(color)
    }

    fun pointChange(on: Boolean) {
        isPointOn = on
        if (on) {
            //   {zh} 风格妆中会修改drawable缓存的填充色，此处使用单独的固定颜色的drawable
            //   {en} Style makeup will modify the fill color of the drawable cache, here use a separate fixed color drawable
            binding.vFaceOptions.setBackgroundResource(R.drawable.dot_point_blue)
        } else {
            binding.vFaceOptions.setBackgroundResource(0)
        }
    }

    fun setMarquee(flag: Boolean) {
        if (binding.tvTitleFaceOptions is MarqueeTextView) {
            binding.tvTitleFaceOptions.setMarqueue(flag)
        }
    }
}