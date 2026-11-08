package com.example.ludo.model

enum class BotDifficulty(val displayName: String, val description: String) {
    EASY("Easy", "Casual moves, occasional mistakes"),
    MEDIUM("Medium", "Balanced play, pursues open tokens"),
    HARD("Hard", "Tactical play, prioritizes captures and safety")
}
