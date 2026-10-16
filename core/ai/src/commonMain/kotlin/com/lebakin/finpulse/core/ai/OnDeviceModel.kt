package com.lebakin.finpulse.core.ai

interface OnDeviceModel {
    suspend fun complete(prompt: String): String
}

class OnDeviceFailed(cause: Throwable? = null) : Exception("On-device model failed", cause)

internal fun onDeviceModelFor(ready: Boolean, model: OnDeviceModel): OnDeviceModel? =
    if (ready) model else null

expect suspend fun openOnDeviceModel(): OnDeviceModel?
