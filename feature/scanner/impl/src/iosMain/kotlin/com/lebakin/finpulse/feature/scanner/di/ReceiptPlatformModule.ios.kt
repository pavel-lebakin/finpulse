package com.lebakin.finpulse.feature.scanner.di

import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.platform.IosReceiptCapture
import com.lebakin.finpulse.feature.scanner.platform.IosReceiptOcr
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun receiptPlatformModule(): Module = module {
    single<ReceiptCapture> { IosReceiptCapture() }
    single<ReceiptOcr> { IosReceiptOcr() }
}
