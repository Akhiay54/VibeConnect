package com.vibeconnect.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.vibeconnect.dto.GroupCreateRequestDTO
import com.vibeconnect.dto.SignInRequestDTO
import com.vibeconnect.dto.UserCreateRequestDTO
import com.vibeconnect.dto.UserUpdateRequestDTO
import com.vibeconnect.services.GroupService
import com.vibeconnect.services.UserService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.mindrot.jbcrypt.BCrypt
import kotlin.collections.hashMapOf

fun Application.configureRouting(userService: UserService, groupService: GroupService) {
    val secret = environment.config.property("jwt.secret").getString()
    val issuer = environment.config.property("jwt.issuer").getString()
    val audience = environment.config.property("jwt.audience").getString()

    routing {
        post("/signUp") {
            val userRequest = call.receive<UserCreateRequestDTO>()
            val id = userService.create(userRequest)
            call.respond(HttpStatusCode.Created, id)
        }

        post("/signIn") {
            val signInRequest = call.receive<SignInRequestDTO>() // Can reuse this DTO
            log.info("Received sign-in request for email: ${signInRequest.email}")
            val result = userService.findByEmailWithPassword(signInRequest.email)
            log.info("Received sign-in request for email: ${signInRequest.email} with result: $result")

            if (result != null) {
                val (user, hashedPassword) = result
                if (BCrypt.checkpw(signInRequest.password, hashedPassword)) {
                    val token = JWT.create()
                        .withAudience(audience)
                        .withIssuer(issuer)
                        .withClaim("email", user.email)
                        .withClaim("userId", user.id)
                        .sign(Algorithm.HMAC256(secret))
                    call.respond(hashMapOf("token" to token))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
                }
            } else {
                call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
            }
        }

        authenticate("jwt") {
            get("/") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.getClaim("userId", Int::class)!!
                call.respondText("Hello user $userId")
            }
            
            get("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.getClaim("userId", Int::class)!!
                val user = userService.read(userId)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            put("/me") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.getClaim("userId", Int::class)!!
                val userRequest = call.receive<UserUpdateRequestDTO>()
                val updatedUser = userService.update(userId, userRequest)
                call.respond(HttpStatusCode.OK, updatedUser)
            }

            get("/users/{id}") {
                val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
                val user = userService.read(id)
                if (user != null) {
                    call.respond(HttpStatusCode.OK, user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
            
            get("/users") {
                val users = userService.getAllUsers()
                call.respond(users)
            }

            // Groups
            post("/groups") {
                val principal = call.principal<JWTPrincipal>()
                val userId = principal!!.getClaim("userId", Int::class)!!
                val groupRequest = call.receive<GroupCreateRequestDTO>()
                val groupId = groupService.create(groupRequest, userId)
                call.respond(HttpStatusCode.Created, mapOf("id" to groupId))
            }

            get("/groups") {
                val groups = groupService.getAllGroups()
                call.respond(groups)
            }
        }
    }
}
