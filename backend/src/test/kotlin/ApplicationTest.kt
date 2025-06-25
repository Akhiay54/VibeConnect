package com.vibeconnect

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*

class ApplicationTest {

    @Test
    fun testSignIn() = testApplication {
        environment {
            config = MapApplicationConfig().apply {
                // JWT configuration
                put("jwt.secret", "secret")
                put("jwt.issuer", "test-issuer")
                put("jwt.audience", "test-audience")
                put("jwt.realm", "test-realm")

                // Database configuration - use H2 in-memory database for testing
                put("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
                put("db.user", "")
                put("db.password", "")
            }
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        application {
            module()
        }

        // Create a test user
        client.post("/signUp") {
            contentType(ContentType.Application.Json)
            setBody("""{"name": "Test User", "email": "test@example.com", "password": "password123"}""")
        }

        // Sign in to get a token
        val response = client.post("/signIn") {
            contentType(ContentType.Application.Json)
            setBody("""{"email": "test@example.com", "password": "password123"}""")
        }

        // Verify we get a successful response with a token
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("token"))

        // Extract token from response
        val responseBody = kotlinx.serialization.json.Json.decodeFromString<Map<String, String>>(response.bodyAsText())
        val token = responseBody["token"]!!

        // Now test an authenticated endpoint
        val authenticatedResponse = client.get("/") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }

        assertEquals(HttpStatusCode.OK, authenticatedResponse.status)
    }
}