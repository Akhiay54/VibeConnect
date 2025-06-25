package com.vibeconnect.db.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object GroupsTable : Table("groups") {
    val id = integer("id").autoIncrement()
    val creatorId = integer("creator_id").references(UserTable.id)
    val name = varchar("name", 255)
    val description = text("description").nullable()
    val coverPhotoUrl = text("cover_photo_url").nullable()
    val isPublic = bool("is_public").default(true)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id)
} 