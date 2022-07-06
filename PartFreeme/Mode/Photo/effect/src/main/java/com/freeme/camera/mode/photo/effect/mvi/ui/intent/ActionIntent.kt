package com.freeme.camera.mode.photo.effect.mvi.ui.intent

sealed class ActionIntent() {
    object Load : ActionIntent()
    object Refresh : ActionIntent()
}