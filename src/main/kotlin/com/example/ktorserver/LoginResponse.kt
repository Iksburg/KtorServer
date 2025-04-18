package com.example.ktorserver

@kotlinx.serialization.Serializable
data class LoginResponse(
    val token: String,
    val userId: Int
)