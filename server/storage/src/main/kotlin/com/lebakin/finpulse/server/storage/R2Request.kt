package com.lebakin.finpulse.server.storage

import java.net.URI
import java.time.Instant

internal const val CACHE_CONTROL = "private, no-store"

internal class SignedCall(
    val method: String,
    val uri: URI,
    val headers: List<Header>,
    val body: ByteArray,
)

internal fun putRequest(endpoint: R2Endpoint, key: String, bytes: ByteArray, instant: Instant): SignedCall =
    signedRequest(endpoint, "PUT", objectUri(endpoint, key), bytes, instant, cacheControl = true)

internal fun headRequest(endpoint: R2Endpoint, instant: Instant): SignedCall =
    signedRequest(endpoint, "HEAD", bucketUri(endpoint), ByteArray(0), instant, cacheControl = false)

internal fun bucketRequest(endpoint: R2Endpoint, instant: Instant): SignedCall =
    signedRequest(endpoint, "PUT", bucketUri(endpoint), ByteArray(0), instant, cacheControl = false)

internal fun objectUri(endpoint: R2Endpoint, key: String): String = "/${endpoint.bucket}/$key"

internal fun bucketUri(endpoint: R2Endpoint): String = "/${endpoint.bucket}"

private fun signedRequest(
    endpoint: R2Endpoint,
    method: String,
    path: String,
    bytes: ByteArray,
    instant: Instant,
    cacheControl: Boolean,
): SignedCall {
    val hash = sha256Hex(bytes)
    val amz = amzDate(instant)
    val dateStamp = amz.substring(0, DATE_STAMP_LENGTH)
    val headers = buildList {
        if (cacheControl) {
            add(Header("cache-control", CACHE_CONTROL))
        }
        add(Header("host", "${endpoint.host}:${endpoint.port}"))
        add(Header("x-amz-content-sha256", hash))
        add(Header("x-amz-date", amz))
    }
    val canonical = canonicalRequest(method, path, "", headers, hash)
    val value = signature(canonical, endpoint.password, dateStamp, amz, endpoint.region)
    val names = headers.sortedBy { it.name }.joinToString(separator = ";") { it.name }
    val auth = authorization(endpoint, dateStamp, names, value)
    val wire = headers.filterNot { it.name == "host" } + Header("Authorization", auth)
    return SignedCall(method, endpointUri(endpoint, path), wire, bytes)
}

private fun endpointUri(endpoint: R2Endpoint, path: String): URI =
    URI("http", null, endpoint.host, endpoint.port, path, null, null)
