package com.example.ktorserver

import kotlinx.serialization.Serializable

@Serializable
data class SleepResponse(
    val title: String,
    val description: String,
    val date: String
)