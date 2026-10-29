package com.lebakin.finpulse.server.storage

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

internal const val SIGN_SECONDS = 60
internal const val DATE_STAMP_LENGTH = 8
internal const val UNSIGNED_PAYLOAD = "UNSIGNED-PAYLOAD"

private const val ALGORITHM = "AWS4-HMAC-SHA256"
private const val HMAC = "HmacSHA256"
private const val SERVICE = "s3"
private const val REQUEST_TYPE = "aws4_request"
private const val BYTE_MASK = 0xFF
private const val NIBBLE_SHIFT = 4
private const val NIBBLE_MASK = 0x0F
private const val HEX = "0123456789abcdef"
private const val HEX_UPPER = "0123456789ABCDEF"

private val amzFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)

internal class Header(val name: String, val value: String)

internal fun presignGet(endpoint: R2Endpoint, key: String, instant: Instant): String {
    val amz = amzDate(instant)
    val dateStamp = amz.substring(0, DATE_STAMP_LENGTH)
    val scope = "$dateStamp/${endpoint.region}/$SERVICE/$REQUEST_TYPE"
    val credential = "${endpoint.accessKey}/$scope"
    val query = "X-Amz-Algorithm=${encode(ALGORITHM)}" +
        "&X-Amz-Credential=${encode(credential)}" +
        "&X-Amz-Date=${encode(amz)}" +
        "&X-Amz-Expires=${encode(SIGN_SECONDS.toString())}" +
        "&X-Amz-SignedHeaders=${encode("host")}"
    val headers = listOf(Header("host", "${endpoint.host}:${endpoint.port}"))
    val canonical = canonicalRequest("GET", objectUri(endpoint, key), query, headers, UNSIGNED_PAYLOAD)
    val value = signature(canonical, endpoint.password, dateStamp, amz, endpoint.region)
    val path = objectUri(endpoint, key)
    return "http://${endpoint.host}:${endpoint.port}$path?$query&X-Amz-Signature=$value"
}

internal fun canonicalRequest(
    method: String,
    uri: String,
    query: String,
    headers: List<Header>,
    payloadHash: String,
): String {
    val sorted = headers.sortedBy { it.name }
    val headerBlock = sorted.joinToString(separator = "") { header -> "${header.name}:${header.value}\n" }
    val signed = sorted.joinToString(separator = ";") { it.name }
    return "$method\n$uri\n$query\n$headerBlock\n$signed\n$payloadHash"
}

internal fun signature(
    canonical: String,
    password: String,
    dateStamp: String,
    amzDate: String,
    region: String,
): String {
    val scope = "$dateStamp/$region/$SERVICE/$REQUEST_TYPE"
    val hashed = sha256Hex(canonical.toByteArray(Charsets.UTF_8))
    val stringToSign = "$ALGORITHM\n$amzDate\n$scope\n$hashed"
    return hex(hmac(signingKey(password, dateStamp, region), stringToSign))
}

internal fun authorization(
    endpoint: R2Endpoint,
    dateStamp: String,
    signedHeaders: String,
    signature: String,
): String {
    val scope = "$dateStamp/${endpoint.region}/$SERVICE/$REQUEST_TYPE"
    val credential = "${endpoint.accessKey}/$scope"
    return "$ALGORITHM Credential=$credential, SignedHeaders=$signedHeaders, Signature=$signature"
}

internal fun amzDate(instant: Instant): String = amzFormatter.format(instant)

internal fun sha256Hex(bytes: ByteArray): String {
    val digest = java.security.MessageDigest.getInstance("SHA-256").digest(bytes)
    return hex(digest)
}

internal fun encode(value: String): String = buildString {
    value.toByteArray(Charsets.UTF_8).forEach { byte ->
        val unsigned = byte.toInt() and BYTE_MASK
        if (unreserved(unsigned)) {
            append(unsigned.toChar())
        } else {
            append('%')
            append(HEX_UPPER[unsigned ushr NIBBLE_SHIFT])
            append(HEX_UPPER[unsigned and NIBBLE_MASK])
        }
    }
}

private fun signingKey(password: String, dateStamp: String, region: String): ByteArray {
    val dateKey = hmac("AWS4$password".toByteArray(Charsets.UTF_8), dateStamp)
    val regionKey = hmac(dateKey, region)
    val serviceKey = hmac(regionKey, SERVICE)
    return hmac(serviceKey, REQUEST_TYPE)
}

private fun hmac(key: ByteArray, data: String): ByteArray {
    val mac = Mac.getInstance(HMAC)
    mac.init(SecretKeySpec(key, HMAC))
    return mac.doFinal(data.toByteArray(Charsets.UTF_8))
}

private fun hex(bytes: ByteArray): String = buildString(bytes.size * 2) {
    bytes.forEach { byte ->
        val unsigned = byte.toInt() and BYTE_MASK
        append(HEX[unsigned ushr NIBBLE_SHIFT])
        append(HEX[unsigned and NIBBLE_MASK])
    }
}

private fun unreserved(value: Int): Boolean {
    val letter = value in 'A'.code..'Z'.code || value in 'a'.code..'z'.code
    val digit = value in '0'.code..'9'.code
    val mark = value == '-'.code || value == '_'.code || value == '.'.code || value == '~'.code
    return letter || digit || mark
}
