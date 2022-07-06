package com.freeme.camera.mode.photo.effect.mvi.ui.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.freeme.camera.mode.photo.effect.databinding.FragmentEffectBinding
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.FragmentVPAdapter
import com.freeme.camera.mode.photo.effect.mvi.ui.intent.ActionIntent
import com.freeme.camera.mode.photo.effect.mvi.ui.viewmodel.EffectViewModel
import kotlinx.coroutines.launch

class EffectFragment : Fragment() {
    private lateinit var binding: FragmentEffectBinding

    private val viewModel: EffectViewModel by viewModels()
    private lateinit var adapter: FragmentVPAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEffectBinding.inflate(inflater);
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.vpBoardContent.adapter = adapter
        lifecycleScope.launch {
            viewModel.actionIntent.send(ActionIntent.Load)
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val fragments: MutableList<Fragment> = mutableListOf()
                    state.ids.forEach {
                        fragments.add(BeautyFaceFragment(it, viewModel.mCallback))
                    }
                }
            }
        }
    }

    fun select(position: Int) {
        binding.vpBoardContent.currentItem = position
    }
}