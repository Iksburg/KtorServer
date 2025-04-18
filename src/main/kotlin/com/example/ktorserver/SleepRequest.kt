package com.example.ktorserver

import kotlinx.serialization.Serializable

@Serializable
data class SleepRequest(
    val id: Int? = null,
    val userId: Int,
    val title: String,
    val description: String,
    val date: String
)