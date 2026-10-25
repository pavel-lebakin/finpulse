package com.lebakin.finpulse.server.gateway

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class GatewayTest {
    @Test
    fun healthAnswersOk() = testApplication {
        application {
            gateway()
        }
        val response = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("ok", response.bodyAsText())
    }

    @Test
    fun otherPathsAreNotFound() = testApplication {
        application {
            gateway()
        }
        val response = client.get("/transactions")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
