package com.lebakin.finpulse.di

import com.lebakin.finpulse.feature.settings.api.LicenseReader
import com.lebakin.finpulse.generated.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

internal class GeneratedLicenseReader : LicenseReader {
    @OptIn(ExperimentalResourceApi::class)
    override suspend fun read(): String = Res.readBytes("files/aboutlibraries.json").decodeToString()
}
