package com.vibeconnect.dto

import kotlinx.serialization.Serializable

@Serializable
data class GroupCreateRequestDTO(
    val name: String,
    val description: String?,
    val isPublic: Boolean = true
)

@Serializable
data class GroupResponseDTO(
    val id: Int,
    val creatorId: Int,
    val name: String,
    val description: String?,
    val coverPhotoUrl: String?,
    val isPublic: Boolean,
    val createdAt: String
) 