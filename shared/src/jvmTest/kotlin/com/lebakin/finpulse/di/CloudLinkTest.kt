package com.lebakin.finpulse.di

import kotlin.test.Test
import kotlin.test.assertEquals

class CloudLinkTest {
    @Test
    fun accountLinkFollowsTheCloudFlag() {
        val linked = runCatching {
            Class.forName("com.lebakin.finpulse.feature.account.di.AccountModuleKt")
        }.isSuccess
        assertEquals(cloudLinked, linked)
    }
}
