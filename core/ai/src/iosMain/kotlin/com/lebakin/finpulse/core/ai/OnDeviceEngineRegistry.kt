package com.lebakin.finpulse.core.ai

import kotlin.experimental.ExperimentalObjCRefinement
import kotlin.native.HiddenFromObjC

private var installedEngine: OnDeviceEngine? = null

fun registerOnDeviceEngine(engine: OnDeviceEngine) {
    installedEngine = engine
}

@OptIn(ExperimentalObjCRefinement::class)
@HiddenFromObjC
fun registeredOnDeviceEngine(): OnDeviceEngine? = installedEngine
