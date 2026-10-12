package com.lebakin.finpulse.feature.scanner.di

import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual fun receiptPlatformModule(): Module = module {
    single<ReceiptCapture> { UnavailableReceiptCapture() }
    single<ReceiptOcr> { UnavailableReceiptOcr() }
}

private class UnavailableReceiptCapture : ReceiptCapture {
    override suspend fun takePhoto(): CaptureOutcome = CaptureOutcome.Unavailable

    override suspend fun pickFromGallery(): CaptureOutcome = CaptureOutcome.Unavailable
}

private class UnavailableReceiptOcr : ReceiptOcr {
    override suspend fun read(image: ReceiptImage): String {
        if (image.bytes.isEmpty()) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
        }
        throw ReceiptUnreadable(ReceiptUnreadable.UNAVAILABLE)
    }
}
