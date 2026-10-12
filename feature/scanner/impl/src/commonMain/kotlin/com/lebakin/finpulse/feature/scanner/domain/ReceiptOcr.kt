package com.lebakin.finpulse.feature.scanner.domain

internal interface ReceiptOcr {
    suspend fun read(image: ReceiptImage): String
}
