package com.lebakin.finpulse.feature.scanner.platform

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import platform.posix.memcpy

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) {
        return NSData()
    }
    return usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    val count = length.toInt()
    if (count == 0) {
        return ByteArray(0)
    }
    val output = ByteArray(count)
    output.usePinned { pinned ->
        memcpy(pinned.addressOf(0), bytes, length)
    }
    return output
}
