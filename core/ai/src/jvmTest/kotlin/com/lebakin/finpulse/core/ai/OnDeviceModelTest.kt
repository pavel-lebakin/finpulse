package com.lebakin.finpulse.core.ai

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlinx.coroutines.test.runTest

class OnDeviceModelTest {
    @Test
    fun anUnregisteredEngineDoesNotPromiseAModel() = runTest {
        assertNull(openOnDeviceModel())
    }

    @Test
    fun aModelExistsOnlyWhenTheEngineIsReady() {
        val model = object : OnDeviceModel {
            override suspend fun complete(prompt: String): String = prompt
        }

        assertNull(onDeviceModelFor(ready = false, model = model))
        assertSame(model, onDeviceModelFor(ready = true, model = model))
    }
}
