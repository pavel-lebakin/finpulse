package com.lebakin.finpulse

import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

@Composable
actual fun AppSystemBars(dark: Boolean) {
    val view = LocalView.current
    val bar = MaterialTheme.colorScheme.background.toArgb()
    if (view.isInEditMode) {
        return
    }
    SideEffect {
        val host = view.context as? ComponentActivity ?: return@SideEffect
        val style = if (dark) {
            SystemBarStyle.dark(bar)
        } else {
            SystemBarStyle.light(bar, bar)
        }
        host.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
}
