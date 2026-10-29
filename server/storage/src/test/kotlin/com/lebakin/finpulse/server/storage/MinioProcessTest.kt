package com.lebakin.finpulse.server.storage

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MinioProcessTest {
    @Test
    fun putThenSign() {
        MinioProcess.start().use { server ->
            openR2(server.endpoint).use { storage ->
                storage.put(KEY, VALUE)
                val url = storage.sign(KEY)
                assertFalse(url.contains(server.endpoint.password))
                assertTrue(url.contains("X-Amz-Expires=$SIGN_SECONDS"))
                val response = fetch(url)
                assertEquals(HTTP_OK, response.status)
                assertEquals(VALUE.toList(), response.body.toList())
                assertTrue(response.cache.contains("private"))
                assertTrue(response.cache.contains("no-store"))
            }
        }
    }

    @Test
    fun blankKeyIsRejected() {
        MinioProcess.start().use { server ->
            openR2(server.endpoint).use { storage ->
                val error = assertFailsWith<IllegalArgumentException> {
                    storage.put("", VALUE)
                }
                assertEquals("R2 key is invalid.", error.message)
            }
        }
    }

    @Test
    fun longObjectIsRejected() {
        MinioProcess.start().use { server ->
            openR2(server.endpoint).use { storage ->
                val error = assertFailsWith<IllegalArgumentException> {
                    storage.put(KEY, ByteArray(OBJECT_LIMIT))
                }
                assertEquals("R2 object is too long.", error.message)
            }
        }
    }

    @Test
    fun wrongPasswordIsRejected() {
        MinioProcess.start().use { server ->
            val password = server.endpoint.password + "x"
            val endpoint = R2Endpoint(
                host = server.endpoint.host,
                port = server.endpoint.port,
                region = server.endpoint.region,
                bucket = server.endpoint.bucket,
                accessKey = server.endpoint.accessKey,
                password = password,
            )
            val error = assertFailsWith<IllegalStateException> {
                openR2(endpoint)
            }
            assertEquals("R2 authentication failed.", error.message)
            assertFalse(error.toString().contains(password))
            assertFalse(error.toString().contains(server.endpoint.password))
        }
    }

    private fun fetch(url: String): SignedResponse = openClient().use { client ->
        val request = HttpRequest.newBuilder(URI.create(url)).GET().timeout(requestTimeout).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofByteArray())
        val cache = response.headers().firstValue("cache-control").orElse("")
        SignedResponse(response.statusCode(), cache, response.body())
    }

    private class SignedResponse(val status: Int, val cache: String, val body: ByteArray)

    private companion object {
        const val KEY = "marker"
        val VALUE = "7".toByteArray(Charsets.UTF_8)
        const val OBJECT_LIMIT = 65_537
    }
}
