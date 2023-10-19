package com.monteoliva.playertest.ui.player

import android.net.Uri
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
fun VideoPlayer(urlMedia: String) {
    VideoPlayer(
        uri = Uri.parse(Uri.decode(urlMedia))
    )
}

@Composable
@OptIn(UnstableApi::class)
private fun VideoPlayer(uri: Uri, modifier: Modifier = Modifier) {
    val context   = LocalContext.current
    val mediaItem = MediaItem.Builder().setUri(uri).build()
    val exoPlayer = remember {
        ExoPlayer.Builder(context)
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
                playWhenReady = true
            }
    }

    val shouldShowControls = remember { mutableStateOf(false) }
    val loading            = remember { mutableStateOf(true)  }
    val title              = remember { mutableStateOf("")    }

    Box(modifier = modifier.background(Color.Black)) {
        DisposableEffect(key1 = Unit) {
            val listener = object: Listener {
                override fun onEvents(player: Player, events: Player.Events) {
                    super.onEvents(player, events)

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
                            title.value   = exoPlayer.mediaMetadata.title.toString()
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
            modifier = Modifier.clickable { shouldShowControls.value = !shouldShowControls.value },
            factory  = {
                PlayerView(context).apply {
                  //useController = false
                    resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    player        = exoPlayer
                    layoutParams  = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                }
            }
        )

        ProgressBar(loading.value)

//        if (!loading.value) {
//            PlayerControl(
//                modifier   = Modifier.fillMaxSize(),
//                isVisible  = { shouldShowControls.value },
//                videoTitle = { title.value }
//            )
//        }
    }
}

@Composable
private fun ShowTitle(modifier: Modifier = Modifier, title: () -> String) {
    val videoTitle = remember(title()) { title() }
    Text(
        modifier = modifier.padding(16.dp),
        text     = videoTitle,
        style    = MaterialTheme.typography.titleLarge,
        color    = Color.White
    )
}

@Composable
private fun PlayerControl(
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean,
    videoTitle: () -> String
) {
    val visible = remember(isVisible()) { isVisible() }

    AnimatedVisibility(
        modifier = modifier,
        visible  = visible,
        enter    = fadeIn(),
        exit     = fadeOut()
    ) {
        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f))) {
            ShowTitle(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth(),
                title = videoTitle
            )
        }
    }
}
