package com.lebakin.finpulse.di

import com.lebakin.finpulse.core.common.AppError
import com.lebakin.finpulse.core.common.ErrorReporter
import com.lebakin.finpulse.core.navigation.ComposeRootNavigator
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.module

expect fun platformModule(): Module

class NoOpErrorReporter : ErrorReporter {
    override fun report(error: AppError) = Unit
}

val appModule = module {
    includes(platformModule())
    single<ErrorReporter> { NoOpErrorReporter() }
    single { ComposeRootNavigator() }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
