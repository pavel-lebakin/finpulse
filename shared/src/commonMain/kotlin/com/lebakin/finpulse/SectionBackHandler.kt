package com.lebakin.finpulse

import androidx.compose.runtime.Composable

@Composable
internal expect fun SectionBackHandler(enabled: Boolean, onBack: () -> Unit)
