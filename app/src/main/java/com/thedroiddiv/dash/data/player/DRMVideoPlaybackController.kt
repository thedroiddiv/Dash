package com.thedroiddiv.dash.data.player


import android.content.Context
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DefaultDrmSessionManager
import androidx.media3.exoplayer.drm.DrmSessionManager
import androidx.media3.exoplayer.drm.FrameworkMediaDrm
import androidx.media3.exoplayer.drm.HttpMediaDrmCallback
import androidx.media3.ui.compose.PlayerSurface
import com.thedroiddiv.dash.domain.models.PlaybackState
import com.thedroiddiv.dash.domain.models.ResolutionInfo
import com.thedroiddiv.dash.domain.player.IDRMVideoPlaybackController
import kotlinx.coroutines.flow.update

@OptIn(UnstableApi::class)
class DRMVideoPlaybackController(
    private val context: Context
) : IDRMVideoPlaybackController {

    private var player: ExoPlayer? = null

    override suspend fun prepare(
        manifestUri: String,
        licenseServerUrl: String,
        callback: IDRMVideoPlaybackController.Callback
    ): ExoPlayer? {
        try {
            player = ExoPlayer.Builder(context).build()
            val httpDataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent(Util.getUserAgent(context, "DRMVideoPlayer"))

            val drmSessionManager = createDrmSessionManager(licenseServerUrl)
            val mediaSource = DashMediaSource.Factory(httpDataSourceFactory)
                .setDrmSessionManagerProvider { mediaItem -> drmSessionManager }
                .createMediaSource(MediaItem.fromUri(manifestUri))
            player!!.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.e(TAG, "Player error: ${error.message}")
                    callback.onError(error.message ?: "Unknown playback error")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    Log.d(TAG, "Playback state changed: $playbackState")
                    val state = when (playbackState) {
                        Player.STATE_IDLE -> PlaybackState.IDLE
                        Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                        Player.STATE_READY -> PlaybackState.READY
                        Player.STATE_ENDED -> PlaybackState.ENDED
                        else -> PlaybackState.UNKNOWN
                    }
                    callback.onStateChanged(state)
                }

                override fun onTracksChanged(tracks: Tracks) {
                    super.onTracksChanged(tracks)
                    val qualities = mutableListOf<ResolutionInfo>()
                    qualities.add(
                        ResolutionInfo(
                            trackIndex = -1,
                            displayName = "Auto",
                            width = 0,
                            height = 0,
                            bitrate = 0,
                            isAuto = true
                        )
                    )
                    for (groupIndex in 0 until tracks.groups.size) {
                        val group = tracks.groups[groupIndex]
                        if (group.type == C.TRACK_TYPE_VIDEO) {
                            for (trackIndex in 0 until group.length) {
                                val format = group.getTrackFormat(trackIndex)
                                val displayName = when {
                                    format.height >= 2160 -> "4K (${format.height}p)"
                                    format.height >= 1440 -> "1440p"
                                    format.height >= 1080 -> "1080p"
                                    format.height >= 720 -> "720p"
                                    format.height >= 480 -> "480p"
                                    format.height >= 360 -> "360p"
                                    format.height >= 240 -> "240p"
                                    else -> "${format.height}p"
                                }

                                qualities.add(
                                    ResolutionInfo(
                                        trackIndex = trackIndex,
                                        displayName = displayName,
                                        width = format.width,
                                        height = format.height,
                                        bitrate = format.bitrate
                                    )
                                )
                            }
                            break
                        }
                    }

                    val sortedQualities =
                        qualities.sortedWith(compareByDescending<ResolutionInfo> { it.isAuto }
                            .thenByDescending { it.height })

                    callback.onTracksChanged(sortedQualities)
                }
            })

            player!!.setMediaSource(mediaSource)
            player!!.prepare()
            player!!.playWhenReady = true

        } catch (e: Exception) {
            Log.e(TAG, "Error creating ExoPlayer", e)
            callback.onError("Failed to create player: ${e.message}")
        }
        return player
    }

    override suspend fun changeResolution(newResolution: ResolutionInfo) {
        player?.let { player -> selectVideoResolution(player, newResolution) }
    }

    override suspend fun release() {
        if (player == null) return
        if (player?.isPlaying == true) player?.stop()
        player?.release()
    }


    @OptIn(UnstableApi::class)
    private fun selectVideoResolution(player: ExoPlayer, resolutionInfo: ResolutionInfo) {
        val trackSelectionParameters = player.trackSelectionParameters

        if (resolutionInfo.isAuto) {
            // Enable automatic quality selection
            player.trackSelectionParameters = trackSelectionParameters
                .buildUpon()
                .clearOverridesOfType(C.TRACK_TYPE_VIDEO)
                .build()
        } else {
            // Select specific quality
            val tracks = player.currentTracks
            for (groupIndex in 0 until tracks.groups.size) {
                val group = tracks.groups[groupIndex]
                if (group.type == C.TRACK_TYPE_VIDEO && resolutionInfo.trackIndex < group.length) {
                    val override = TrackSelectionOverride(
                        group.mediaTrackGroup,
                        listOf(resolutionInfo.trackIndex)
                    )

                    player.trackSelectionParameters = trackSelectionParameters
                        .buildUpon()
                        .setOverrideForType(override)
                        .build()
                    break
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createDrmSessionManager(licenseServerUrl: String): DrmSessionManager {
        return try {
            val licenseCallback = HttpMediaDrmCallback(
                licenseServerUrl,
                DefaultHttpDataSource.Factory()
            )

            DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(
                    C.WIDEVINE_UUID,
                    FrameworkMediaDrm.DEFAULT_PROVIDER
                )
                .build(licenseCallback)

        } catch (e: Exception) {
            Log.e(TAG, "Error creating DRM session manager", e)
            DrmSessionManager.DRM_UNSUPPORTED
        }
    }

    companion object {
        const val TAG = "DRMVideoPlaybackController"
    }
}
