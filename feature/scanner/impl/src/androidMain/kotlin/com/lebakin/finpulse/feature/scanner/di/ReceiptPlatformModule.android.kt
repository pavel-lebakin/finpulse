package com.lebakin.finpulse.feature.scanner.di

import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.platform.AndroidReceiptCapture
import com.lebakin.finpulse.feature.scanner.platform.AndroidReceiptOcr
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun receiptPlatformModule(): Module = module {
    single { AndroidReceiptCapture() }
    single<ReceiptCapture> { get<AndroidReceiptCapture>() }
    single<ReceiptOcr> { AndroidReceiptOcr() }
}
