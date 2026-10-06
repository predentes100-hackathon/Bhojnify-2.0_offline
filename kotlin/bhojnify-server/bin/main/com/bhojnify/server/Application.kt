package com.bhojnify.server

import com.bhojnify.core.repository.InMemoryMessRepository
import com.bhojnify.core.repository.JsonFileMessRepository
import com.bhojnify.core.repository.MessRepository
import com.bhojnify.server.plugins.configureHTTP
import com.bhojnify.server.plugins.configureMonitoring
import com.bhojnify.server.plugins.configureRouting
import com.bhojnify.server.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import java.io.File

fun main(args: Array<String>) {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 5000
    val host = "0.0.0.0"

    println("Starting Bhojnify Kotlin API Server on http://$host:$port")

    embeddedServer(Netty, port = port, host = host) {
        val storageFile = File(System.getProperty("user.home"), ".bhojnify/state.json")
        val repository: MessRepository = JsonFileMessRepository(storageFile)
        module(repository)
    }.start(wait = true)
}

fun Application.module(repository: MessRepository = InMemoryMessRepository()) {
    configureSerialization()
    configureHTTP()
    configureMonitoring()
    configureRouting(repository)
}
