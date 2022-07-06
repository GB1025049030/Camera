package com.freeme.camera.mode.photo.effect.mvi.ui.intent

sealed class ActionState {
    object Load : ActionState()
    object ItemClick : ActionState()
    object Refresh : ActionState()
}