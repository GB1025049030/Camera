package com.freeme.camera.mode.photo.effect.mvi.bean

import android.os.Parcelable
import com.freeme.camera.mode.photo.effect.utils.MathUtils
import kotlinx.parcelize.Parcelize

@Parcelize
data class ButtonItem @JvmOverloads constructor(
    var id: Int = 0,
    val icon: Int = 0,
    val title: Int = 0,
    val desc: Int = 0,
    var isEnableMultiSelect: Boolean = false,
    var isReuseChildrenIntensity: Boolean = false,
    var isSelected: Boolean = false,
    var isEnableNegative: Boolean = false,
    var node: ComposerNode? = null,
    var parent: ButtonItem? = null,
    var selectChild: ButtonItem? = null,
    var children: List<ButtonItem> = listOf(),
    var colorItems: List<ColorItem> = listOf()
) : Parcelable {

    constructor(id: Int, children: List<ButtonItem>) : this(id) {
        this.children = children
    }

    constructor(id: Int, icon: Int, title: Int, node: ComposerNode?) : this(id, icon, title) {
        this.node = node
    }

    constructor(id: Int, icon: Int, title: Int, children: List<ButtonItem>) : this(
        id,
        icon,
        title
    ) {
        this.children = children
    }

    constructor(
        id: Int,
        icon: Int,
        title: Int,
        children: List<ButtonItem>,
        enableMultiSelect: Boolean
    ) : this(id, icon, title) {
        this.children = children
        this.isEnableMultiSelect = enableMultiSelect
    }

    constructor(
        id: Int,
        icon: Int,
        title: Int,
        node: ComposerNode?,
        colorItems: List<ColorItem>
    ) : this(id, icon, title) {
        this.node = node
        this.colorItems = colorItems
    }

    constructor(
        id: Int,
        icon: Int,
        title: Int,
        desc: Int,
        node: ComposerNode?,
        colorItems: List<ColorItem>
    ) : this(id, icon, title, desc) {
        this.node = node
        this.colorItems = colorItems
    }


//
//    constructor(id: Int, children: List<ButtonItem>, enableMultiSelect: Boolean) : this(id) {
//        this.children = children
//        this.isEnableMultiSelect = enableMultiSelect
//    }
//

//

//
//    constructor(
//        id: Int,
//        icon: Int,
//        title: Int,
//        node: ComposerNode?,
//        colorItems: List<ColorItem>
//    ) : this(id, title, icon) {
//        this.id = id
//        this.node = node
//        this.colorItems = colorItems
//    }
//
//    constructor(
//        id: Int,
//        icon: Int,
//        title: Int,
//        desc: Int,
//        node: ComposerNode?,
//        test: Int,
//        colorItems: List<ColorItem>
//    ) : this(id, title, icon, desc, node) {
//        this.colorItems = colorItems
//    }
//
//    constructor(
//        id: Int,
//        icon: Int,
//        title: Int,
//        node: ComposerNode?,
//        enableNegative: Boolean
//    ) : this(id, title, icon) {
//        this.node = node
//        this.isEnableNegative = enableNegative
//    }
//
//    constructor(
//        id: Int,
//        icon: Int,
//        title: Int,
//        desc: Int,
//        node: ComposerNode?,
//        enableNegative: Boolean
//    ) : this(id, title, icon, desc, node) {
//        this.isEnableNegative = enableNegative
//    }
//
//    constructor(
//        id: Int,
//        icon: Int,
//        title: Int,
//        children: List<ButtonItem>,
//        enableMultiSelect: Boolean
//    ) : this(id, title, icon) {
//        this.children = children
//        this.isEnableMultiSelect = enableMultiSelect
//    }

    init {
        updateChildren()
    }

    val validIntensity: FloatArray
        get() {
            return if (selectChild == null) {
                return if (node == null) FloatArray(0) else node!!.intensityArray
            } else selectChild!!.validIntensity
        }

    val availableItem: ButtonItem?
        get() {
            if (!hasChildren()) {
                return if (node == null) null else this
            }
            return if (selectChild == null) null else selectChild!!.availableItem
        }

    var intensityArray: FloatArray?
        get() = if (node == null) FloatArray(0) else node!!.intensityArray
        set(intensityArray) {
            if (node == null) return
            node!!.intensityArray = intensityArray!!
        }

    fun hasChildren(): Boolean {
        return children.isNotEmpty()
    }

    fun hasIntensity(): Boolean {
        var self = false
        if (intensityArray!!.isNotEmpty()) {
            self = if (isEnableNegative) {
                !MathUtils.floatEqual(
                    intensityArray!![0], 0.5f
                )
            } else {
                intensityArray!![0] > 0
            }
        }
        var child = false
        for (item in children) {
            if (item.hasIntensity()) {
                child = true
            }
        }
        return self || child
    }

    //  {zh} 是否高亮  {en} Whether to highlight
    fun shouldHighLight(): Boolean {
        return parent!!.selectChild === this
    }

    //  {zh} 是否显示小圆点  {en} Whether to show small dots
    fun shouldPointOn(): Boolean {
        return parent!!.isEnableMultiSelect && isSelected && hasIntensity()
    }

    private fun updateChildren() {
        for (child in children) {
            child.parent = this
        }
        selectChild = children[0]
    }
}
