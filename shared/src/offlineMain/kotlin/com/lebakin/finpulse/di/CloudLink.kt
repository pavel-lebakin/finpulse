package com.lebakin.finpulse.di

import org.koin.core.module.Module

internal actual val cloudLinked: Boolean = false

internal actual fun cloudModules(): List<Module> = emptyList()
