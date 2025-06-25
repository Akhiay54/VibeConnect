package com.vibeconnect.plugins

import com.vibeconnect.db.tables.GroupMembersTable
import com.vibeconnect.db.tables.GroupsTable
import com.vibeconnect.db.tables.UserTable
import com.vibeconnect.services.GroupService
import com.vibeconnect.services.UserService
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases(): Pair<UserService, GroupService> {
    val database = Database.connect(
        url = environment.config.property("db.url").getString(),
        user = environment.config.property("db.user").getString(),
        driver = "org.postgresql.Driver",
        password = environment.config.property("db.password").getString()
    )

    transaction(database) {
        SchemaUtils.create(UserTable, GroupsTable, GroupMembersTable)
    }

    val userService = UserService(database)
    val groupService = GroupService(database)
    return Pair(userService, groupService)
}
