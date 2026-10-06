package com.bhojnify.server.plugins

import com.bhojnify.core.repository.MessRepository
import com.bhojnify.server.routes.bhojnifyApiRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(repository: MessRepository) {
    routing {
        bhojnifyApiRoutes(repository)
    }
}
