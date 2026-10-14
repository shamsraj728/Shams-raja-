package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val username: String,
    val email: String,
    val passwordHash: String,
    val fullName: String = "",
    val avatarUrl: String = "",
    val timeZone: String = "UTC",
    val bio: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthState(
    val currentUser: UserEntity? = null,
    val isAuthenticated: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
