package com.thedroiddiv.dash.ui.screen.details

import com.thedroiddiv.dash.domain.models.Video

data class UiState(
    val video: Video,
    val error: String? = null
)
