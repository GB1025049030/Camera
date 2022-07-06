package com.freeme.camera.mode.photo.effect.mvi.ui.view

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freeme.camera.mode.photo.effect.R
import com.freeme.camera.mode.photo.effect.databinding.FragmentItemViewPageBinding
import com.freeme.camera.mode.photo.effect.mvi.ui.adapter.BaseRVAdapter

open class ItemViewPageFragment<A : BaseRVAdapter<*>?> : Fragment() {
    var adapter: A? = null
        protected set
    var mSelectedPadding = 0
        protected set
    var mItemDecoration: RecyclerView.ItemDecoration? = null
        protected set

    lateinit var binding: FragmentItemViewPageBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentItemViewPageBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (adapter != null) {
            initRVView()
        }
    }

    private fun initRVView() {
        binding.rvItemViewPage.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.rvItemViewPage.adapter = adapter
        if (mItemDecoration != null) {
            binding.rvItemViewPage.addItemDecoration(mItemDecoration!!)
        } else {
            binding.rvItemViewPage.addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val position = parent.getChildAdapterPosition(view)
                    val totalCount = parent.adapter!!.itemCount
                    val rvMarginHorizontal =
                        (resources.getDimensionPixelSize(R.dimen.rv_margin_horizontal)
                                - mSelectedPadding)
                    val space =
                        (resources.getDimensionPixelSize(R.dimen.item_distance) - 2 * mSelectedPadding)
                    when (position) {
                        0 -> { // {zh} 第一个 {en} The first
                            outRect.left = rvMarginHorizontal
                            outRect.right = space / 2
                        }
                        totalCount - 1 -> {
                            outRect.left = space / 2
                            outRect.right = rvMarginHorizontal
                        }
                        else -> { // {zh} 中间其它的 {en} Other in the middle
                            outRect.left = space / 2
                            outRect.right = space / 2
                        }
                    }
                }
            })
        }
    }

    fun refreshUI() {
        if (adapter == null) return
        adapter!!.notifyDataSetChanged()
    }
}