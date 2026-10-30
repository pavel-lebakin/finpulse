package com.lebakin.finpulse.di

import org.koin.core.module.Module

internal expect val cloudLinked: Boolean

internal expect fun cloudModules(): List<Module>
