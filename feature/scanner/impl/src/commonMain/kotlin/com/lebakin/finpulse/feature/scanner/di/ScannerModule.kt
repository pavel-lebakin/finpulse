package com.lebakin.finpulse.feature.scanner.di

import com.lebakin.finpulse.feature.scanner.api.ScannerEntry
import com.lebakin.finpulse.feature.scanner.domain.ReceiptClock
import com.lebakin.finpulse.feature.scanner.domain.ReceiptIds
import com.lebakin.finpulse.feature.scanner.domain.ReceiptParser
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSession
import com.lebakin.finpulse.feature.scanner.domain.SystemReceiptClock
import com.lebakin.finpulse.feature.scanner.domain.SystemReceiptIds
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerEntryImpl
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigator
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigatorImpl
import com.lebakin.finpulse.feature.scanner.presentation.viewmodel.CaptureScreenViewModel
import com.lebakin.finpulse.feature.scanner.presentation.viewmodel.ReviewScreenViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val scannerModule = module {
    includes(receiptPlatformModule())
    single<ReceiptClock> { SystemReceiptClock() }
    single<ReceiptIds> { SystemReceiptIds() }
    single { ReceiptParser(get()) }
    single { ReceiptSession() }
    single<ScannerNavigator> { ScannerNavigatorImpl(get()) }
    single<ScannerEntry> { ScannerEntryImpl(get()) }
    viewModelOf(::CaptureScreenViewModel)
    viewModelOf(::ReviewScreenViewModel)
}
