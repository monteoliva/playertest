package com.monteoliva.playertest

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint

import com.monteoliva.playertest.ui.player.VideoPlayer
import com.monteoliva.playertest.ui.theme.PlayerTestTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayerTestTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background
                ) {
                    InitView()
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean =
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            finish()
            true
        }
        else {
            super.onKeyDown(keyCode, event)
        }
}

@Composable
private fun InitView() {
    val urlMedia = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
    VideoPlayer(
        uri = Uri.parse(Uri.decode(urlMedia))
    )
}