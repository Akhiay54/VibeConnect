package com.vibeconnect.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class UserCreateRequestDTO(
    val name: String,
    val email: String,
    val password: String
)

@Serializable
data class UserUpdateRequestDTO(
    val name: String,
    val email: String,
    val password: String? = null
)

@Serializable
data class SignInRequestDTO(val email: String, val password: String) 