package com.lebakin.finpulse.core.ai

actual suspend fun openOnDeviceModel(): OnDeviceModel? = openInstalledEngine(registeredOnDeviceEngine())
