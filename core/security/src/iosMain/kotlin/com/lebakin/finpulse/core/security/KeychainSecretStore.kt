package com.lebakin.finpulse.core.security

import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cstr
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDataRef
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFMutableDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleWhenUnlockedThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecReturnData
import platform.Security.kSecValueData
import platform.posix.memcpy

internal actual val secretDispatcher: CoroutineDispatcher = Dispatchers.Default

actual fun openSecretStore(): SecretStore = KeychainSecretStore()

@OptIn(ExperimentalForeignApi::class)
private class KeychainSecretStore : SecretStore {
    private val mutex = Mutex()
    private val saved = MutableStateFlow(itemExists())

    override fun observeSaved(): Flow<Boolean> = saved

    override suspend fun save(secret: String) {
        require(secret.isNotBlank()) { "Secret is empty." }
        mutex.withLock {
            deleteItem()
            val status = addItem(secret.encodeToByteArray())
            check(status == errSecSuccess) { "Keychain save failed ($status)." }
            saved.value = true
        }
    }

    override suspend fun read(): String? = mutex.withLock {
        readItem()
    }

    override suspend fun delete() {
        mutex.withLock {
            deleteItem()
            saved.value = false
        }
    }

    private fun itemExists(): Boolean = readItem() != null
}

@OptIn(ExperimentalForeignApi::class)
private fun addItem(secret: ByteArray): Int {
    val service = cfString(SERVICE)
    val account = cfString(ACCOUNT)
    val data = secret.toCFData()
    val query = newQuery(service, account)
    return try {
        CFDictionaryAddValue(query, kSecValueData, data)
        CFDictionaryAddValue(query, kSecAttrAccessible, kSecAttrAccessibleWhenUnlockedThisDeviceOnly)
        SecItemAdd(query, null)
    } finally {
        release(query)
        release(service)
        release(account)
        release(data)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun readItem(): String? {
    val service = cfString(SERVICE)
    val account = cfString(ACCOUNT)
    val query = newQuery(service, account)
    return try {
        CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
        memScoped {
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            when (status) {
                errSecItemNotFound -> null
                errSecSuccess -> copySecret(result.value)
                else -> error("Keychain read failed ($status).")
            }
        }
    } finally {
        release(query)
        release(service)
        release(account)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun deleteItem() {
    val service = cfString(SERVICE)
    val account = cfString(ACCOUNT)
    val query = newQuery(service, account)
    try {
        val status = SecItemDelete(query)
        check(status == errSecSuccess || status == errSecItemNotFound) {
            "Keychain delete failed ($status)."
        }
    } finally {
        release(query)
        release(service)
        release(account)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun newQuery(service: CFStringRef?, account: CFStringRef?): CFMutableDictionaryRef? {
    val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null)
    CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
    CFDictionaryAddValue(query, kSecAttrService, service)
    CFDictionaryAddValue(query, kSecAttrAccount, account)
    return query
}

@OptIn(ExperimentalForeignApi::class)
private fun cfString(value: String): CFStringRef? = memScoped {
    CFStringCreateWithCString(kCFAllocatorDefault, value.cstr.ptr, kCFStringEncodingUTF8)
}

@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toCFData(): CFDataRef? {
    if (isEmpty()) {
        return CFDataCreate(kCFAllocatorDefault, null, 0)
    }
    return usePinned { pinned ->
        CFDataCreate(
            kCFAllocatorDefault,
            pinned.addressOf(0).reinterpret<UByteVar>(),
            size.toLong(),
        )
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun copySecret(value: CPointer<*>?): String {
    val data = requireNotNull(value) { "Keychain read failed." }
    try {
        val length = CFDataGetLength(data.reinterpret()).toInt()
        if (length == 0) {
            return ""
        }
        val bytes = ByteArray(length)
        bytes.usePinned { pinned ->
            memcpy(pinned.addressOf(0), CFDataGetBytePtr(data.reinterpret()), length.toULong())
        }
        return bytes.decodeToString()
    } finally {
        release(value)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun release(value: CPointer<*>?) {
    if (value != null) {
        CFRelease(value)
    }
}

private const val SERVICE = "com.lebakin.finpulse"
private const val ACCOUNT = "byok"
