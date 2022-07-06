package com.freeme.camera.mode.photo.effect.mvi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freeme.camera.mode.photo.effect.manager.EffectDataManager
import com.freeme.camera.mode.photo.effect.model.EffectType
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem
import com.freeme.camera.mode.photo.effect.mvi.bean.EffectUiState
import com.freeme.camera.mode.photo.effect.mvi.repository.EffectRepository
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.IBeautyCallBack
import com.freeme.camera.mode.photo.effect.mvi.ui.intent.ActionIntent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class EffectViewModel(
    private val repository: EffectRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EffectUiState())
    val uiState: StateFlow<EffectUiState> = _uiState

    val actionIntent = Channel<ActionIntent>(Channel.UNLIMITED)

    init {
        initActionIntent()
    }

    private fun initActionIntent() {
        viewModelScope.launch {
            actionIntent.consumeAsFlow().collect {
                when (it) {
                    ActionIntent.Load -> initIds()
                }
            }
        }
    }

    private fun initIds() {
        viewModelScope.launch {
            val ids: List<Int> = listOf(
                EffectDataManager.TYPE_BEAUTY_FACE,
                EffectDataManager.TYPE_MAKEUP
            )
            _uiState.update {
                it.copy(ids = ids)
            }
        }
    }

    var mCallback = object : IBeautyCallBack {
        override fun onEffectItemClick(item: ButtonItem?) {
            TODO("Not yet implemented")
        }

        override fun onEffectItemClose(item: ButtonItem?) {
            TODO("Not yet implemented")
        }

        override fun getEffectType(): EffectType? {
            return EffectType.LITE_ASIA
        }
    }

}