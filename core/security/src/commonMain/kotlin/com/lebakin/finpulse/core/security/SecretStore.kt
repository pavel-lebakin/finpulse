package com.lebakin.finpulse.core.security

import kotlinx.coroutines.flow.Flow

interface SecretStore {
    fun observeSaved(): Flow<Boolean>

    suspend fun save(secret: String)

    suspend fun read(): String?

    suspend fun delete()
}

expect fun openSecretStore(): SecretStore
