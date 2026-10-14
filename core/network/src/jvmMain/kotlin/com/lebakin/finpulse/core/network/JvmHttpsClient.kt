package com.lebakin.finpulse.core.network

import io.ktor.client.engine.okhttp.OkHttp

actual fun openHttpsClient(): HttpsClient = DefaultHttpsClient(
    KtorHttpExchange(httpsEngineClient(OkHttp.create())),
)
