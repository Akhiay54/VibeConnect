package com.vibeconnect.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object GroupMembersTable : Table("group_members") {
    val groupId = integer("group_id").references(GroupsTable.id)
    val userId = integer("user_id").references(UserTable.id)
    val role = varchar("role", 20) // e.g., "admin", "member"
    val joinedAt = timestamp("joined_at")

    override val primaryKey = PrimaryKey(groupId, userId)
} 