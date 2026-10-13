package com.lebakin.finpulse.core.security

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal actual val secretDispatcher: CoroutineDispatcher = Dispatchers.IO

actual fun openSecretStore(): SecretStore = MemorySecretStore()

private class MemorySecretStore : SecretStore {
    private val saved = MutableStateFlow(false)
    private var value: String? = null

    override fun observeSaved(): Flow<Boolean> = saved

    override suspend fun save(secret: String) {
        require(secret.isNotBlank()) { "Secret is empty." }
        value = secret
        saved.value = true
    }

    override suspend fun read(): String? = value

    override suspend fun delete() {
        value = null
        saved.value = false
    }
}
