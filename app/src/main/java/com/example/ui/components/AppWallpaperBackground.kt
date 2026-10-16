package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.MaterialTheme
import java.io.File

@Composable
fun AppWallpaperBackground(
    wallpaperUri: String,
    darkness: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val file = remember(wallpaperUri) {
            if (wallpaperUri.isNotBlank()) File(wallpaperUri) else null
        }
        // Wallpapers are copied into app storage by SettingsScreen. A missing
        // file is not a wallpaper: retain the active theme background rather
        // than showing a stale URI or a hard-coded image.
        if (file?.isFile == true) {
            AsyncImage(
                model = file,
                contentDescription = "App Background Wallpaper",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = darkness.coerceIn(0f, 0.9f)))
            )
        }

        content()
    }
}
