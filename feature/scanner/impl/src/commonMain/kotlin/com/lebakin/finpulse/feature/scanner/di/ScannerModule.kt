package com.lebakin.finpulse.feature.scanner.di

import com.lebakin.finpulse.core.ai.ByokModel
import com.lebakin.finpulse.core.ai.OnDeviceModel
import com.lebakin.finpulse.core.ai.openByokModel
import com.lebakin.finpulse.core.ai.openOnDeviceModel
import com.lebakin.finpulse.core.network.HttpsClient
import com.lebakin.finpulse.core.preferences.ConsentPreferences
import com.lebakin.finpulse.core.security.SecretStore
import com.lebakin.finpulse.feature.scanner.api.ScannerEntry
import com.lebakin.finpulse.feature.scanner.domain.ReceiptClock
import com.lebakin.finpulse.feature.scanner.domain.ReceiptEngines
import com.lebakin.finpulse.feature.scanner.domain.ReceiptIds
import com.lebakin.finpulse.feature.scanner.domain.ReceiptModel
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
    single {
        val store = get<SecretStore>()
        val client = get<HttpsClient>()
        val consent = get<ConsentPreferences>()
        ReceiptEngines(
            rules = get(),
            onDevice = { openOnDeviceModel().asReceiptModel() },
            byok = { openByokModel(store, client, consent).asReceiptModel() },
        )
    }
    single { ReceiptSession() }
    single<ScannerNavigator> { ScannerNavigatorImpl(get()) }
    single<ScannerEntry> { ScannerEntryImpl(get()) }
    viewModelOf(::CaptureScreenViewModel)
    viewModelOf(::ReviewScreenViewModel)
}

private fun OnDeviceModel?.asReceiptModel(): ReceiptModel? =
    this?.let { model -> ReceiptModel { prompt -> model.complete(prompt) } }

private fun ByokModel?.asReceiptModel(): ReceiptModel? =
    this?.let { model -> ReceiptModel { prompt -> model.complete(prompt) } }
