package com.lebakin.finpulse.di

import com.lebakin.finpulse.feature.account.di.accountModule
import org.koin.core.module.Module

internal actual val cloudLinked: Boolean = true

internal actual fun cloudModules(): List<Module> = listOf(accountModule)
