package com.lebakin.finpulse.core.network

import io.ktor.client.engine.darwin.Darwin

actual fun openHttpsClient(): HttpsClient = DefaultHttpsClient(
    KtorHttpExchange(httpsEngineClient(Darwin.create())),
)
