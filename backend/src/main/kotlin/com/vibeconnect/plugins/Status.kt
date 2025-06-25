package com.vibeconnect.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val message: String?)

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is IllegalArgumentException -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(cause.message))
                else -> call.respond(HttpStatusCode.InternalServerError, ErrorResponse(cause.message))
            }
        }
    }
} 