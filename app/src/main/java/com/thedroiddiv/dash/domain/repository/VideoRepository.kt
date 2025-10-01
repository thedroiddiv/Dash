package com.thedroiddiv.dash.domain.repository

import com.thedroiddiv.dash.domain.models.Video

interface VideoRepository {
    suspend fun fetchVideoDetails(
        videoId: String,
        userToken: String
    ): Result<Video>
}