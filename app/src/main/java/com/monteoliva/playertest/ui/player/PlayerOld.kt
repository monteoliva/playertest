package com.monteoliva.playertest.ui.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.lifecycle.viewmodel.compose.viewModel

import com.monteoliva.playertest.ui.components.ProgressBar
import com.monteoliva.playertest.ui.player.controls.PlayerControls

@Composable
@OptIn(UnstableApi::class)
fun VideoScreenOld(
    uri: Uri,
    displayTitle: String,
    viewModel: PlayerViewModel = viewModel()
) {
    val context   = LocalContext.current
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context)
            .apply {
                setSeekBackIncrementMs(PLAYER_SEEK_BACK_INCREMENT)
                setSeekForwardIncrementMs(PLAYER_SEEK_FORWARD_INCREMENT)
            }
            .build()
    }

    VideoPlayer(
        context      = context,
        exoPlayer    = exoPlayer,
        uri          = uri,
        displayTitle = displayTitle
    )
}

@Composable
@OptIn(UnstableApi::class)
private fun VideoPlayer(
    modifier: Modifier = Modifier,
    context: Context,
    exoPlayer: ExoPlayer,
    uri: Uri,
    displayTitle: String
) {
    exoPlayer.apply {
        addMediaSource(playerMediaSource(uri = uri, displayTitle = displayTitle))
        prepare()
        playWhenReady = true
        videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        //repeatMode       = REPEAT_MODE_OFF
    }

    val shouldShowControls = remember { mutableStateOf(false) }
    val loading            = remember { mutableStateOf(true) }
    val title              = remember { mutableStateOf("") }
    val isPlaying          = remember { mutableStateOf(exoPlayer.isPlaying) }
    val isFullScreen       = remember { mutableStateOf(true) }
    val totalDuration      = remember { mutableStateOf(0L) }
    val currentTime        = remember { mutableStateOf(0L) }
    val bufferedPercentage = remember { mutableStateOf(0) }
    val playbackState      = remember { mutableStateOf(exoPlayer.playbackState) }

    val currentPosition = rememberSaveable { mutableStateOf(0L) }
    val playerView      = rememberPlayerViewWithLifecycle(exoPlayer) { currentPosition.value = it }

    Box(modifier = modifier.background(Color.Black)) {
        DisposableEffect(playerView) {
            val listener = object : Listener {
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
                    Log.d("PlayerTest", "playWhenReady: $playWhenReady - reason: $reason")
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
                    Log.d("PlayerTest", "Error: ${error.message}")
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
            factory  = { playerView }
        ) {
            it.player?.seekTo(currentPosition.value)
        }

        if (!loading.value) {
            PlayerControls(
                modifier      = Modifier.fillMaxSize(),
                isVisible     = { shouldShowControls.value },
                isPlaying     = { isPlaying.value },
                title         = { title.value },
                playbackState = { playbackState.value },
                onReplayClick = {
                    exoPlayer.seekBack()
                    shouldShowControls.value = shouldShowControls.value.not()
                },
                onForwardClick = {
                    exoPlayer.seekForward()
                    shouldShowControls.value = shouldShowControls.value.not()
                },
                onPauseToggle = {
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
                    isFullScreen.value = isFullScreen.value.not()
                    shouldShowControls.value = shouldShowControls.value.not()
                }
            )
        }

        ProgressBar(loading.value)
    }
}

private const val PLAYER_SEEK_BACK_INCREMENT    = 5 * 1000L // 5 seconds
private const val PLAYER_SEEK_FORWARD_INCREMENT = 10 * 1000L // 10 seconds