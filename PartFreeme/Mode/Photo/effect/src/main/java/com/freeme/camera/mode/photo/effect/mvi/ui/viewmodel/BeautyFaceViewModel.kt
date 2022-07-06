package com.freeme.camera.mode.photo.effect.mvi.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freeme.camera.mode.photo.effect.manager.EffectDataManager
import com.freeme.camera.mode.photo.effect.mvi.bean.BeautyFaceUiState
import com.freeme.camera.mode.photo.effect.mvi.bean.ButtonItem
import com.freeme.camera.mode.photo.effect.mvi.repository.EffectRepository
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.IBeautyCallBack
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.OnItemClickListener
import com.freeme.camera.mode.photo.effect.mvi.ui.intent.ActionIntent
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BeautyFaceViewModel(
    private val repository: EffectRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BeautyFaceUiState())
    val uiState: StateFlow<BeautyFaceUiState> = _uiState

    var type = -1
    var callback: IBeautyCallBack? = null

    private lateinit var mItemGroup: ButtonItem

    val actionIntent = Channel<ActionIntent>(Channel.UNLIMITED)
    private var fetchJob: Job? = null

    val itemClickListener: OnItemClickListener = object : OnItemClickListener {
        override fun onItemClick(item: ButtonItem, position: Int) {
            if (item.id == EffectDataManager.TYPE_CLOSE) {
                removeOrAddItem(repository.mSelectNodes, item.parent!!, false)
            } else {
                if (!repository.mSelectNodes.contains(item) && !item.hasChildren()) {
                    var itemIntensity: FloatArray? = null
                    if (!mItemGroup.isEnableMultiSelect) {
                        if (mItemGroup.selectChild != null) {
                            val itemToRemove: ButtonItem = mItemGroup.selectChild!!
                            if (mItemGroup.isReuseChildrenIntensity && itemToRemove.id != EffectDataManager.TYPE_CLOSE) {
                                itemIntensity = itemToRemove.intensityArray?.clone()
                            }
                            removeOrAddItem(repository.mSelectNodes, itemToRemove, false)
                        }
                    }
                    if (item.node != null) {
                        if (itemIntensity == null) {
                            itemIntensity = EffectDataManager.getDefaultIntensity(
                                item.id,
                                callback!!.getEffectType(),
                                item.isEnableNegative
                            )
                        }
                        if (itemIntensity != null && item.intensityArray != null) {
                            var i = 0
                            while (i < itemIntensity.size && i < item.intensityArray!!.size) {
                                itemIntensity[i].also { item.intensityArray!![i] = it }
                                i++
                            }
                        }
                        removeOrAddItem(repository.mSelectNodes, item, true)
                    }
                }
            }
            mItemGroup.selectChild = item
            callback?.onEffectItemClick(item)
            viewModelScope.launch {
                val items = items(mItemGroup)
                _uiState.update {
                    it.copy(items = items)
                }
            }
        }
    }

    init {
        initActionIntent()
    }

    private fun initActionIntent() {
        viewModelScope.launch {
            actionIntent.consumeAsFlow().collect {
                when (it) {
                    ActionIntent.Load -> fetchItems(type)
                    ActionIntent.Refresh -> refreshSelectedItem()
                }
            }
        }
    }

    private fun fetchItems(type: Int) {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            mItemGroup = repository.getItem(type)!!
            _uiState.update {
                it.copy(items = items(mItemGroup))
            }
        }
    }

    private fun removeOrAddItem(set: MutableSet<ButtonItem>, item: ButtonItem, add: Boolean) {
        if (add) {
            if (item.availableItem != null) {
                set.add(item)
                item.isSelected = true
            }
        } else {
            item.selectChild = null
            set.remove(item)
            item.isSelected = false
            callback?.onEffectItemClose(item)
            if (item.hasChildren()) {
                item.children.forEach {
                    removeOrAddItem(set, it, false)
                }
            }
        }
    }

    private fun items(item: ButtonItem): List<ButtonItem> {
        return if (item.hasChildren()) {
            item.children
        } else listOf(item)
    }

    private fun refreshSelectedItem() {
        if (!mItemGroup.isSelected) {
            mItemGroup.selectChild = mItemGroup.children[0]
        }
    }
}