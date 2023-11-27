package com.example.interstellarvoyage

data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val username: String,
    val timeCompleted: Double
)