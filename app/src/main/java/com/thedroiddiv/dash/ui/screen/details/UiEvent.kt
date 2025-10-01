package com.thedroiddiv.dash.ui.screen.details

sealed interface UiEvent {
    data class LoadVideoDetailsClicked(val videoId:String, val userToken:String): UiEvent
}