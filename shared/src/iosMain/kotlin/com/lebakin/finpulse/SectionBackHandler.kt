package com.lebakin.finpulse

import androidx.compose.runtime.Composable

@Composable
internal actual fun SectionBackHandler(enabled: Boolean, onBack: () -> Unit) {
    if (enabled) {
        return
    }
    onBack
}
