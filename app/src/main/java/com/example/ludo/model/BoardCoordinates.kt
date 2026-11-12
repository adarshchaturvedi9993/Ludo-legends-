package com.example.ludo.model

data class GridCoord(val row: Float, val col: Float)

object BoardCoordinates {
    const val GRID_SIZE = 15

    // 52 perimeter tiles indexed 0 to 51
    val TRACK_TILES: List<GridCoord> = listOf(
        // 0..4 (Red arm top edge going right)
        GridCoord(6f, 1f), // 0: Red Start (Safe Star)
        GridCoord(6f, 2f), // 1
        GridCoord(6f, 3f), // 2
        GridCoord(6f, 4f), // 3
        GridCoord(6f, 5f), // 4
        // 5..10 (Green arm left edge going up)
        GridCoord(5f, 6f), // 5
        GridCoord(4f, 6f), // 6
        GridCoord(3f, 6f), // 7
        GridCoord(2f, 6f), // 8: Safe Star
        GridCoord(1f, 6f), // 9
        GridCoord(0f, 6f), // 10
        // 11 (Top apex)
        GridCoord(0f, 7f), // 11
        // 12..17 (Green arm right edge going down)
        GridCoord(0f, 8f), // 12
        GridCoord(1f, 8f), // 13: Green Start (Safe Star)
        GridCoord(2f, 8f), // 14
        GridCoord(3f, 8f), // 15
        GridCoord(4f, 8f), // 16
        GridCoord(5f, 8f), // 17
        // 18..23 (Yellow arm top edge going right)
        GridCoord(6f, 9f),  // 18
        GridCoord(6f, 10f), // 19
        GridCoord(6f, 11f), // 20
        GridCoord(6f, 12f), // 21: Safe Star
        GridCoord(6f, 13f), // 22
        GridCoord(6f, 14f), // 23
        // 24 (Right apex)
        GridCoord(7f, 14f), // 24
        // 25..30 (Yellow arm bottom edge going left)
        GridCoord(8f, 14f), // 25
        GridCoord(8f, 13f), // 26: Yellow Start (Safe Star)
        GridCoord(8f, 12f), // 27
        GridCoord(8f, 11f), // 28
        GridCoord(8f, 10f), // 29
        GridCoord(8f, 9f),  // 30
        // 31..36 (Blue arm right edge going down)
        GridCoord(9f, 8f),  // 31
        GridCoord(10f, 8f), // 32
        GridCoord(11f, 8f), // 33
        GridCoord(12f, 8f), // 34: Safe Star
        GridCoord(13f, 8f), // 35
        GridCoord(14f, 8f), // 36
        // 37 (Bottom apex)
        GridCoord(14f, 7f), // 37
        // 38..43 (Blue arm left edge going up)
        GridCoord(14f, 6f), // 38
        GridCoord(13f, 6f), // 39: Blue Start (Safe Star)
        GridCoord(12f, 6f), // 40
        GridCoord(11f, 6f), // 41
        GridCoord(10f, 6f), // 42
        GridCoord(9f, 6f),  // 43
        // 44..49 (Red arm bottom edge going left)
        GridCoord(8f, 5f), // 44
        GridCoord(8f, 4f), // 45
        GridCoord(8f, 3f), // 46
        GridCoord(8f, 2f), // 47: Safe Star
        GridCoord(8f, 1f), // 48
        GridCoord(8f, 0f), // 49
        // 50 (Left apex)
        GridCoord(7f, 0f), // 50
        // 51 (Red loop end)
        GridCoord(6f, 0f)  // 51
    )

    val SAFE_TILE_INDICES: Set<Int> = setOf(0, 8, 13, 21, 26, 34, 39, 47)

    fun isSafeTile(trackIndex: Int): Boolean = SAFE_TILE_INDICES.contains(trackIndex)

    // Home corridor coordinates (step 1..5)
    val RED_HOME: List<GridCoord> = listOf(
        GridCoord(7f, 1f),
        GridCoord(7f, 2f),
        GridCoord(7f, 3f),
        GridCoord(7f, 4f),
        GridCoord(7f, 5f)
    )

    val GREEN_HOME: List<GridCoord> = listOf(
        GridCoord(1f, 7f),
        GridCoord(2f, 7f),
        GridCoord(3f, 7f),
        GridCoord(4f, 7f),
        GridCoord(5f, 7f)
    )

    val YELLOW_HOME: List<GridCoord> = listOf(
        GridCoord(7f, 13f),
        GridCoord(7f, 12f),
        GridCoord(7f, 11f),
        GridCoord(7f, 10f),
        GridCoord(7f, 9f)
    )

    val BLUE_HOME: List<GridCoord> = listOf(
        GridCoord(13f, 7f),
        GridCoord(12f, 7f),
        GridCoord(11f, 7f),
        GridCoord(10f, 7f),
        GridCoord(9f, 7f)
    )

    // Yard slot coordinates for 4 tokens per color
    val RED_YARD_SLOTS: List<GridCoord> = listOf(
        GridCoord(1.8f, 1.8f),
        GridCoord(1.8f, 3.8f),
        GridCoord(3.8f, 1.8f),
        GridCoord(3.8f, 3.8f)
    )

    val GREEN_YARD_SLOTS: List<GridCoord> = listOf(
        GridCoord(1.8f, 10.8f),
        GridCoord(1.8f, 12.8f),
        GridCoord(3.8f, 10.8f),
        GridCoord(3.8f, 12.8f)
    )

    val YELLOW_YARD_SLOTS: List<GridCoord> = listOf(
        GridCoord(10.8f, 10.8f),
        GridCoord(10.8f, 12.8f),
        GridCoord(12.8f, 10.8f),
        GridCoord(12.8f, 12.8f)
    )

    val BLUE_YARD_SLOTS: List<GridCoord> = listOf(
        GridCoord(10.8f, 1.8f),
        GridCoord(10.8f, 3.8f),
        GridCoord(12.8f, 1.8f),
        GridCoord(12.8f, 3.8f)
    )

    // Central victory triangle positions (step 6)
    val FINISH_GOAL: Map<PlayerColor, GridCoord> = mapOf(
        PlayerColor.RED to GridCoord(7.0f, 6.2f),
        PlayerColor.GREEN to GridCoord(6.2f, 7.0f),
        PlayerColor.YELLOW to GridCoord(7.0f, 7.8f),
        PlayerColor.BLUE to GridCoord(7.8f, 7.0f)
    )

    /**
     * Neat 2x2 layout inside each color's center goal triangle for finished pawns (order 1..4).
     * Prevents stacking and renders each pawn in its dedicated finish quadrant.
     */
    fun finishedSlot(color: PlayerColor, order: Int): GridCoord {
        val slotIndex = (order - 1).coerceIn(0, 3)
        return when (color) {
            PlayerColor.RED -> when (slotIndex) {
                0 -> GridCoord(6.70f, 6.20f)
                1 -> GridCoord(6.70f, 6.50f)
                2 -> GridCoord(7.30f, 6.20f)
                else -> GridCoord(7.30f, 6.50f)
            }
            PlayerColor.GREEN -> when (slotIndex) {
                0 -> GridCoord(6.25f, 6.75f)
                1 -> GridCoord(6.25f, 7.25f)
                2 -> GridCoord(6.55f, 6.75f)
                else -> GridCoord(6.55f, 7.25f)
            }
            PlayerColor.YELLOW -> when (slotIndex) {
                0 -> GridCoord(6.75f, 7.45f)
                1 -> GridCoord(6.75f, 7.75f)
                2 -> GridCoord(7.25f, 7.45f)
                else -> GridCoord(7.25f, 7.75f)
            }
            PlayerColor.BLUE -> when (slotIndex) {
                0 -> GridCoord(7.45f, 6.75f)
                1 -> GridCoord(7.45f, 7.25f)
                2 -> GridCoord(7.75f, 6.75f)
                else -> GridCoord(7.75f, 7.25f)
            }
        }
    }

    fun getCoordinate(token: Token): GridCoord {
        return when (token.state) {
            TokenState.YARD -> {
                val slots = when (token.color) {
                    PlayerColor.RED -> RED_YARD_SLOTS
                    PlayerColor.GREEN -> GREEN_YARD_SLOTS
                    PlayerColor.YELLOW -> YELLOW_YARD_SLOTS
                    PlayerColor.BLUE -> BLUE_YARD_SLOTS
                }
                slots[token.id.coerceIn(0, 3)]
            }
            TokenState.TRACK -> {
                val trackIdx = token.color.stepToTrackIndex(token.stepsTraveled)
                TRACK_TILES[trackIdx]
            }
            TokenState.HOME_STRETCH -> {
                val homeList = when (token.color) {
                    PlayerColor.RED -> RED_HOME
                    PlayerColor.GREEN -> GREEN_HOME
                    PlayerColor.YELLOW -> YELLOW_HOME
                    PlayerColor.BLUE -> BLUE_HOME
                }
                val idx = (token.homeStep - 1).coerceIn(0, homeList.size - 1)
                homeList[idx]
            }
            TokenState.FINISHED -> {
                val order = if (token.finishOrder in 1..4) token.finishOrder else (token.id + 1)
                finishedSlot(token.color, order)
            }
        }
    }

    /**
     * Calculates the sequence of tile coordinates for an authentic hop-by-hop movement.
     */
    fun computeHopPath(token: Token, roll: Int): List<GridCoord> {
        val path = mutableListOf<GridCoord>()
        val startCoord = getCoordinate(token)
        path.add(startCoord)

        when (token.state) {
            TokenState.YARD -> {
                if (roll == 6) {
                    path.add(TRACK_TILES[token.color.startIndex])
                }
            }
            TokenState.TRACK -> {
                for (step in 1..roll) {
                    val currSteps = token.stepsTraveled + step
                    if (currSteps <= 50) {
                        path.add(TRACK_TILES[token.color.stepToTrackIndex(currSteps)])
                    } else {
                        val homeStep = currSteps - 50
                        val homeList = when (token.color) {
                            PlayerColor.RED -> RED_HOME
                            PlayerColor.GREEN -> GREEN_HOME
                            PlayerColor.YELLOW -> YELLOW_HOME
                            PlayerColor.BLUE -> BLUE_HOME
                        }
                        if (homeStep in 1..5) {
                            path.add(homeList[homeStep - 1])
                        } else if (homeStep == 6) {
                            val order = if (token.finishOrder in 1..4) token.finishOrder else (token.id + 1)
                            path.add(finishedSlot(token.color, order))
                        }
                    }
                }
            }
            TokenState.HOME_STRETCH -> {
                val homeList = when (token.color) {
                    PlayerColor.RED -> RED_HOME
                    PlayerColor.GREEN -> GREEN_HOME
                    PlayerColor.YELLOW -> YELLOW_HOME
                    PlayerColor.BLUE -> BLUE_HOME
                }
                for (step in 1..roll) {
                    val targetHomeStep = token.homeStep + step
                    if (targetHomeStep in 1..5) {
                        path.add(homeList[targetHomeStep - 1])
                    } else if (targetHomeStep == 6) {
                        val order = if (token.finishOrder in 1..4) token.finishOrder else (token.id + 1)
                        path.add(finishedSlot(token.color, order))
                    }
                }
            }
            TokenState.FINISHED -> {}
        }
        return path
    }
}
