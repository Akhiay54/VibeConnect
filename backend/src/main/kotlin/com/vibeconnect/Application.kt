package com.vibeconnect

import com.vibeconnect.plugins.*
import com.vibeconnect.services.GroupService
import com.vibeconnect.services.UserService
import io.ktor.server.application.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    val (userService, groupService) = configureDatabases()
    configureSecurity()
    configureSerialization()
    configureStatusPages()
    configureRouting(userService, groupService)
}
