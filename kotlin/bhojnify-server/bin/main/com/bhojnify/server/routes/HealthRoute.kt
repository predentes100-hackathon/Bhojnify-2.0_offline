package com.bhojnify.server.routes

import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class HealthStatus(
    val status: String
)

fun Route.healthRoutes() {
    get("/healthz") {
        call.respond(HttpStatusCode.OK, HealthStatus(status = "ok"))
    }
}
