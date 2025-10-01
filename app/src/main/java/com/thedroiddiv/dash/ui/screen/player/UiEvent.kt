package com.thedroiddiv.dash.ui.screen.player

import com.thedroiddiv.dash.domain.models.ResolutionInfo

sealed interface UiEvent {
    data object ChangeResolutionClicked : UiEvent
    data class ResolutionSelected(val resolution: ResolutionInfo) : UiEvent
    data object ResolutionDialogDismissed : UiEvent

    data object RetryClicked: UiEvent
}