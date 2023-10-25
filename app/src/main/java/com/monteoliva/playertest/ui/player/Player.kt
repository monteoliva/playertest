package com.monteoliva.playertest.ui.player

import android.net.Uri
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

import com.monteoliva.playertest.ui.components.ProgressBar

@Composable
@OptIn(UnstableApi::class)
fun VideoPlayer(
    uri: Uri,
    modifier: Modifier = Modifier,
    displayTitle: String
) {
    val context   = LocalContext.current
    val mediaItem = MediaItem.Builder().apply {
        setUri(uri)
        setMediaMetadata(
            MediaMetadata.Builder().setDisplayTitle(displayTitle).build()
        )
    }
    .build()
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .apply {
                setSeekBackIncrementMs(PLAYER_SEEK_BACK_INCREMENT)
                setSeekForwardIncrementMs(PLAYER_SEEK_FORWARD_INCREMENT)
            }
            .build()
            .apply {
                val defaultDataSourceFactory = DefaultDataSource.Factory(context)
                val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
                    context,
                    defaultDataSourceFactory
                )
                val source = ProgressiveMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(mediaItem)

                setMediaSource(source, 0)
                prepare()
                playWhenReady    = true
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
                repeatMode       = REPEAT_MODE_OFF
            }
    }

    val shouldShowControls = remember { mutableStateOf(false) }
    val loading            = remember { mutableStateOf(true)  }
    val title              = remember { mutableStateOf("")    }
    val isPlaying          = remember { mutableStateOf(exoPlayer.isPlaying) }
    val isFullScreen       = remember { mutableStateOf(true) }
    val totalDuration      = remember { mutableStateOf(0L) }
    val currentTime        = remember { mutableStateOf(0L) }
    val bufferedPercentage = remember { mutableStateOf(0) }
    val playbackState      = remember { mutableStateOf(exoPlayer.playbackState) }

    Box(modifier = modifier.background(Color.Black)) {
        DisposableEffect(key1 = Unit) {
            val listener = object: Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)
                    player.also {
                        totalDuration.value      = it.duration.coerceAtLeast(0L)
                        currentTime.value        = it.currentPosition.coerceAtLeast(0L)
                        bufferedPercentage.value = it.bufferedPercentage
                        isPlaying.value          = it.isPlaying
                        playbackState.value      = it.playbackState
                    }
                }
                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    Log.d("PlayerTest","playWhenReady: $playWhenReady - reason: $reason")
                }
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE      -> {}
                        Player.STATE_ENDED     -> {}
                        Player.STATE_BUFFERING -> {
                            title.value   = ""
                            loading.value = true
                        }
                        Player.STATE_READY -> {
                            loading.value = false
                            title.value   = exoPlayer.mediaMetadata.displayTitle.toString()
                        }
                    }
                }
                override fun onPlayerError(error: PlaybackException) {
                    Log.d("PlayerTest","Error: ${error.message}")
                }
            }

            exoPlayer.addListener(listener)

            onDispose {
                exoPlayer.removeListener(listener)
                exoPlayer.release()
            }
        }

        AndroidView(
            modifier = Modifier.clickable { shouldShowControls.value = shouldShowControls.value.not() },
            factory  = {
                PlayerView(context).apply {
                    useController = false
                    player        = exoPlayer
                    layoutParams  = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                    if (isFullScreen.value) {
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    }
                }
            }
        )

        if (!loading.value) {
            PlayerControl(
                modifier       = Modifier.fillMaxSize(),
                isVisible      = { shouldShowControls.value },
                isPlaying      = { isPlaying.value },
                title          = { title.value },
                playbackState  = { playbackState.value },
                onReplayClick  = {
                    exoPlayer.seekBack()
                    shouldShowControls.value = shouldShowControls.value.not()
                },
                onForwardClick = {
                    exoPlayer.seekForward()
                    shouldShowControls.value = shouldShowControls.value.not()
                },
                onPauseToggle  = {
                    when {
                        exoPlayer.isPlaying -> exoPlayer.pause()
                        exoPlayer.isPlaying.not() && playbackState.value == STATE_ENDED -> {
                            exoPlayer.seekTo(0)
                            exoPlayer.playWhenReady = true
                        }
                        else -> {
                            exoPlayer.play()
                            shouldShowControls.value = shouldShowControls.value.not()
                        }
                    }
                    isPlaying.value = isPlaying.value.not()
                },
                totalDuration      = { totalDuration.value },
                currentTime        = { currentTime.value },
                bufferedPercentage = { bufferedPercentage.value },
                onSeekChanged      = { timeMs: Float -> exoPlayer.seekTo(timeMs.toLong()) },
                onFullScreen       = {
                    isFullScreen.value       = isFullScreen.value.not()
                    shouldShowControls.value = shouldShowControls.value.not()
                }
            )
        }

        ProgressBar(loading.value)
    }
}

private const val PLAYER_SEEK_BACK_INCREMENT    = 5 * 1000L // 5 seconds
private const val PLAYER_SEEK_FORWARD_INCREMENT = 10 * 1000L // 10 seconds