package com.lebakin.finpulse

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.scene.Scene

private const val NAV_MOTION_MILLIS = 250

private val navMotion = tween<IntOffset>(
    durationMillis = NAV_MOTION_MILLIS,
    easing = FastOutSlowInEasing,
)

internal fun <T : Any> opaquePush(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    slideInHorizontally(navMotion) { width -> width } togetherWith
        slideOutHorizontally(navMotion) { width -> -width }
}

internal fun <T : Any> opaquePop(): AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    slideInHorizontally(navMotion) { width -> -width } togetherWith
        slideOutHorizontally(navMotion) { width -> width }
}

internal fun <T : Any> opaquePredictivePop(): AnimatedContentTransitionScope<Scene<T>>.(Int) -> ContentTransform = {
    slideInHorizontally(navMotion) { width -> -width } togetherWith
        slideOutHorizontally(navMotion) { width -> width }
}
