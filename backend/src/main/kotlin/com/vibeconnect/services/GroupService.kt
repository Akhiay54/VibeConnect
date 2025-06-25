package com.vibeconnect.services

import com.vibeconnect.db.tables.GroupMembersTable
import com.vibeconnect.db.tables.GroupsTable
import com.vibeconnect.dto.GroupCreateRequestDTO
import com.vibeconnect.dto.GroupResponseDTO
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.Instant

class GroupService(private val database: Database) {

    private fun toGroupResponseDTO(row: ResultRow) = GroupResponseDTO(
        id = row[GroupsTable.id],
        creatorId = row[GroupsTable.creatorId],
        name = row[GroupsTable.name],
        description = row[GroupsTable.description],
        coverPhotoUrl = row[GroupsTable.coverPhotoUrl],
        isPublic = row[GroupsTable.isPublic],
        createdAt = row[GroupsTable.createdAt].toString()
    )

    suspend fun create(group: GroupCreateRequestDTO, creatorId: Int): Int = newSuspendedTransaction {
        val groupId = GroupsTable.insert {
            it[this.creatorId] = creatorId
            it[name] = group.name
            it[description] = group.description
            it[isPublic] = group.isPublic
            it[createdAt] = Instant.now()
        } get GroupsTable.id

        // The creator automatically becomes a member and an admin
        GroupMembersTable.insert {
            it[this.groupId] = groupId
            it[userId] = creatorId
            it[role] = "admin"
            it[joinedAt] = Instant.now()
        }
        groupId
    }

    suspend fun getAllGroups(): List<GroupResponseDTO> = newSuspendedTransaction {
        GroupsTable.selectAll().map(::toGroupResponseDTO)
    }
} 