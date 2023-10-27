package com.monteoliva.playertest.ui.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource

@Composable
@OptIn(UnstableApi::class)
fun playerMediaSource(uri: Uri, displayTitle: String) : MediaSource {
    val context                  = LocalContext.current
    val mediaItem                = getMediaItem(uri = uri, displayTitle = displayTitle)
    val defaultDataSourceFactory = DefaultDataSource.Factory(context)
    val dataSourceFactory: DataSource.Factory = DefaultDataSource.Factory(
        context,
        defaultDataSourceFactory
    )

    return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(mediaItem)
}

@Composable
private fun getMediaItem(uri: Uri, displayTitle: String) : MediaItem {
    val mediaMetadata = MediaMetadata.Builder().setDisplayTitle(displayTitle).build()
    return MediaItem
        .Builder()
        .setUri(uri)
        .setMediaMetadata(mediaMetadata)
        .build()
}