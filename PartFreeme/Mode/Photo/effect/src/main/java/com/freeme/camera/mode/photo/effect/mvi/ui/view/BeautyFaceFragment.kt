package com.freeme.camera.mode.photo.effect.mvi.ui.view

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.EffectRVAdapter
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.IBeautyCallBack
import com.freeme.camera.mode.photo.effect.mvi.ui.intent.ActionIntent
import com.freeme.camera.mode.photo.effect.mvi.ui.viewmodel.BeautyFaceViewModel
import kotlinx.coroutines.launch

class BeautyFaceFragment(
    type: Int,
    callback: IBeautyCallBack
) : ItemViewPageFragment<EffectRVAdapter>() {
    private val viewModel: BeautyFaceViewModel by viewModels()

    init {
        if (adapter == null) {
            adapter = EffectRVAdapter(viewModel.itemClickListener)
        }
        viewModel.type = type
        viewModel.callback = callback
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycleScope.launch {
            viewModel.actionIntent.send(ActionIntent.Load)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    adapter!!.setItemList(it.items)
                    refreshUI()
                }
            }
        }
    }
}