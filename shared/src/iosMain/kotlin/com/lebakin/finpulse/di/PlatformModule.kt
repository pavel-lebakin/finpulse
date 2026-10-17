package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.ai.OnDeviceEngine
import com.lebakin.finpulse.core.ai.registeredOnDeviceEngine
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModule(): Module = module {
    registeredOnDeviceEngine()?.let { engine ->
        single<OnDeviceEngine> { engine }
    }
}
