package com.thedroiddiv.dash.domain.models

data class ResolutionInfo(
    val trackIndex: Int,
    val displayName: String,
    val width: Int,
    val height: Int,
    val bitrate: Int,
    val isAuto: Boolean = false
)