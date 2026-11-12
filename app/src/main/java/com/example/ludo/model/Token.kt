package com.example.ludo.model

enum class TokenState {
    YARD,
    TRACK,
    HOME_STRETCH,
    FINISHED
}

data class Token(
    val id: Int,
    val color: PlayerColor,
    val state: TokenState = TokenState.YARD,
    val stepsTraveled: Int = 0, // 0..50 on the main perimeter
    val homeStep: Int = 0,      // 1..5 in home corridor, 6 is FINISHED
    val finishOrder: Int = 0    // 1..4 when finished, 0 if not yet finished
) {
    val isFinished: Boolean get() = state == TokenState.FINISHED
    val isInYard: Boolean get() = state == TokenState.YARD
    val isOnTrack: Boolean get() = state == TokenState.TRACK
    val isInHomeStretch: Boolean get() = state == TokenState.HOME_STRETCH

    /**
     * Total progress from 0 (in yard) to 57 (finished).
     */
    val totalProgress: Int
        get() = when (state) {
            TokenState.YARD -> 0
            TokenState.TRACK -> 1 + stepsTraveled
            TokenState.HOME_STRETCH -> 51 + homeStep
            TokenState.FINISHED -> 57
        }

    val currentTrackIndex: Int?
        get() = if (state == TokenState.TRACK) {
            color.stepToTrackIndex(stepsTraveled)
        } else null
}
