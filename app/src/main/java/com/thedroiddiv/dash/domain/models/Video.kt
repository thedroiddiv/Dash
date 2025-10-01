package com.thedroiddiv.dash.domain.models

sealed class Video(
    val thumbnail: String,
    val title: String,
    val description: String
) {
    class Show(
        val episodes: List<Episode>,
        thumbnail: String,
        title: String,
        description: String
    ) : Video(thumbnail, title, description)

    class Movie(
        thumbnail: String,
        title: String,
        description: String
    ) : Video(thumbnail, title, description)
}

