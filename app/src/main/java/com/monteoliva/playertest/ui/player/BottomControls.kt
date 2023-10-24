package com.monteoliva.playertest.ui.player

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import java.util.concurrent.TimeUnit

import com.monteoliva.playertest.R

@Composable
fun BottomControls(
    modifier: Modifier = Modifier,
    totalDuration: () -> Long,
    currentTime: () -> Long,
    bufferedPercentage: () -> Int,
    onSeekChanged: (timeMs: Float) -> Unit,
    onFullScreen: () -> Unit
) {

    val duration  = remember(totalDuration()) { totalDuration() }
    val videoTime = remember(currentTime()) { currentTime() }
    val buffer    = remember(bufferedPercentage()) { bufferedPercentage() }
    val current   = remember(currentTime()) { currentTime() }

    Column(modifier = modifier.padding(bottom = 32.dp)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Slider(
                value         = buffer.toFloat(),
                enabled       = false,
                onValueChange = {},
                valueRange    = 0f..100f,
                colors        = SliderDefaults.colors(
                    disabledThumbColor       = Color.Transparent,
                    disabledActiveTrackColor = Color.Gray
                )
            )

            Slider(
                modifier      = Modifier.fillMaxWidth(),
                value         = videoTime.toFloat(),
                onValueChange = onSeekChanged,
                valueRange    = 0f..duration.toFloat(),
                colors        = SliderDefaults.colors(
                    thumbColor      = Color.White,
                    activeTickColor = Color.Red
                )
            )
        }

        Row(
            modifier              = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text     = "${current.formatMinSec()}/${duration.formatMinSec()}",
                color    = Color.White
            )

            IconButton(
                modifier = Modifier.padding(horizontal = 16.dp),
                onClick  = onFullScreen
            ) {
                Image(
                    contentScale       = ContentScale.Crop,
                    painter            = painterResource(id = R.drawable.ic_fullscreen),
                    contentDescription = "Enter/Exit fullscreen"
                )
            }
        }
    }
}

fun Long.formatMinSec(): String {
    return if (this == 0L) { "..." }
    else {
        String.format(
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(this),
            TimeUnit.MILLISECONDS.toSeconds(this) -
                    TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(this)
                    )
        )
    }
}
