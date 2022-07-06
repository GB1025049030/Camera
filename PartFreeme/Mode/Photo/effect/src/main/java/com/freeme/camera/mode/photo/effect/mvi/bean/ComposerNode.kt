package com.freeme.camera.mode.photo.effect.mvi.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ComposerNode @JvmOverloads constructor(
    var path: String,
    var keyArray: Array<String> = arrayOf(),
    var tag: String? = null,
    var intensityArray: FloatArray = FloatArray(0)
) : Parcelable {

    constructor(path: String, key: String, intensity: Float) : this(
        path,
        arrayOf(key),
        null,
        floatArrayOf(intensity)
    )

    constructor(path: String, keyArray: Array<String>, intensity: Float) : this(
        path,
        keyArray,
        null,
        floatArrayOf(intensity)
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ComposerNode

        if (path != other.path) return false
        if (!keyArray.contentEquals(other.keyArray)) return false
        if (tag != other.tag) return false
        if (!intensityArray.contentEquals(other.intensityArray)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = path.hashCode()
        result = 31 * result + keyArray.contentHashCode()
        result = 31 * result + (tag?.hashCode() ?: 0)
        result = 31 * result + intensityArray.contentHashCode()
        return result
    }
}