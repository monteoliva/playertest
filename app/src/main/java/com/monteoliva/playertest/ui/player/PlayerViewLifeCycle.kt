package com.monteoliva.playertest.ui.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
@OptIn(UnstableApi::class)
fun rememberPlayerViewWithLifecycle(exoPlayer: ExoPlayer, onDisposeCalled: (Long) -> Unit): PlayerView {
    val context    = LocalContext.current
    val playerView = remember {
        PlayerView(context).apply {
            useController = true
            player        = exoPlayer
            resizeMode    = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            layoutParams  = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }
    val lifecycleObserver = rememberPlayerLifecycleObserver(playerView)
    val lifecycle         = LocalLifecycleOwner.current.lifecycle

    DisposableEffect(lifecycle) {
        lifecycle.addObserver(lifecycleObserver)
        onDispose {
            val position = playerView.player?.currentPosition ?: 0
            onDisposeCalled(position)
            lifecycle.removeObserver(lifecycleObserver)
        }
    }
    return playerView
}

@Composable
private fun rememberPlayerLifecycleObserver(player: PlayerView): LifecycleEventObserver = remember(player) {
    LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_RESUME  -> player.onResume()
            Lifecycle.Event.ON_PAUSE   -> player.onPause()
            Lifecycle.Event.ON_DESTROY -> player.player?.release()
            else                       -> {}
        }
    }
}