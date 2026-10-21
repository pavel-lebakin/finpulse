package com.lebakin.finpulse.feature.settings.api

interface LicenseReader {
    suspend fun read(): String
}
