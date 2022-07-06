package com.freeme.camera.mode.photo.effect.mvi.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.freeme.camera.mode.photo.effect.mvi.ui.view.BeautyFaceFragment

class FragmentVPAdapter @JvmOverloads constructor(
    fragment: Fragment,
    private var mFragments: List<Fragment> = listOf(),
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int {
        return mFragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragments[position]
    }

    fun setFragments(fragments: List<Fragment>) {
        mFragments = fragments
        notifyDataSetChanged()
    }
}