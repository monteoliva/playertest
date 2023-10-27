package com.monteoliva.playertest.ui.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.monteoliva.playertest.ui.player.control.TopControls

@Composable
fun ShowTitle(
    modifier: Modifier = Modifier,
    isVisible: () -> Boolean,
    title:     () -> String
) {
    val visible = remember(isVisible()) { isVisible() }
    AnimatedVisibility(
        modifier = modifier,
        visible  = visible,
        enter    = fadeIn(),
        exit     = fadeOut()
    ) {
        Box(modifier = Modifier.background(Color.Black.copy(alpha = 0.6f))) {
            TopControls(
                modifier = Modifier.align(Alignment.TopStart).fillMaxWidth(),
                title = title
            )
        }
    }
}