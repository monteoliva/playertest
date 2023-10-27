package com.monteoliva.playertest.ui.player.control

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopControls(
    modifier: Modifier = Modifier,
    title: () -> String
) {
    val videoTitle = remember(title()) { title() }
    Text(
        modifier = modifier.padding(16.dp),
        text     = videoTitle,
        style    = MaterialTheme.typography.titleLarge,
        color    = Color.White
    )
}