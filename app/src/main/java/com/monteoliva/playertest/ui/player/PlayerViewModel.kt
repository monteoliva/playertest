package com.monteoliva.playertest.ui.player

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(context: Context) : ViewModel() {
//    private val _exoPlayer = MutableStateFlow(ExoPlayer.Builder(context).build())
//    val exoPlayer: StateFlow<ExoPlayer> = _exoPlayer.asStateFlow()
}