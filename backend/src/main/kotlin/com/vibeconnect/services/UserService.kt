package com.vibeconnect.services

import com.vibeconnect.db.tables.UserTable
import com.vibeconnect.dto.UserCreateRequestDTO
import com.vibeconnect.dto.UserResponseDTO
import com.vibeconnect.dto.UserUpdateRequestDTO
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt

class UserService(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(UserTable)
        }
    }

    private fun toUserResponseDTO(row: ResultRow): UserResponseDTO {

        exposedLogger.info("Converting ResultRow to UserResponseDTO ${row[UserTable.id]}")
        return UserResponseDTO(
            id = row[UserTable.id],
            name = row[UserTable.name],
            email = row[UserTable.email]
        )
    }

    suspend fun create(user: UserCreateRequestDTO): Int = newSuspendedTransaction {
        val insertResult = UserTable.insert {
            it[name] = user.name
            it[email] = user.email
            it[password] = BCrypt.hashpw(user.password, BCrypt.gensalt())
        }
        insertResult[UserTable.id]
    }

    suspend fun findByEmail(email: String): UserResponseDTO? = newSuspendedTransaction {
        UserTable.select(UserTable.email.eq(email))
            .map(::toUserResponseDTO)
            .singleOrNull()
    }


    // This is a more internal-facing method, so it's okay to have the password here
    suspend fun findByEmailWithPassword(email: String): Pair<UserResponseDTO, String>? = newSuspendedTransaction {
        UserTable.selectAll().where { UserTable.email eq email }
            .map { exposedLogger.info("Found user with email: $email with id: $it")
                toUserResponseDTO(it) to it[UserTable.password] }
            .singleOrNull()
    }

    suspend fun read(id: Int): UserResponseDTO? = newSuspendedTransaction {
        UserTable.selectAll().where{ UserTable.id eq id }
            .map(::toUserResponseDTO)
            .singleOrNull()
    }

    suspend fun update(id: Int, user: UserUpdateRequestDTO): UserResponseDTO {
        newSuspendedTransaction {
            UserTable.update({ UserTable.id eq id }) {
                it[name] = user.name
                it[email] = user.email
                if (!user.password.isNullOrBlank()) {
                    it[password] = BCrypt.hashpw(user.password, BCrypt.gensalt())
                }
            }
        }
        return read(id)!!
    }

    suspend fun getAllUsers(): List<UserResponseDTO> = newSuspendedTransaction {
        UserTable.selectAll().map(::toUserResponseDTO)
    }
} 