# Implementation Plan: In-Game Economy, Customization Shop & 7-Day Daily Login System

## 1. Overview & Core Deliverables

This plan outlines the architecture, data structures, UI flows, and ad validation mechanics for adding a comprehensive player progression economy to **Ludo Legends**:

- **Currency Economy**: In-game Coins balance permanently stored in `GamePreferences`.
- **Rewarded Ad Earnings**: Watch a rewarded video ad to earn **50 Coins** (strictly validated via Google AdMob's `onRewardEarned` callback; never rewarded on mere button click).
- **6 New Premium Pawn Colors**: 25 coins each (Ruby Royal, Neon Cyan, Emerald Jade, Imperial Amethyst, Rose Quartz, Amber Sunset), adding to the 4 default unlocked colors (Red, Green, Yellow, Blue).
- **5 Exclusive Ludo Board Themes**: 150 coins each (Royal Gold, Midnight Obsidian, Cyberpunk Neon, Classic Wooden Parchment, Nebula Galaxy) plus the default Royal Navy theme.
- **Shop & Customization Placement**: Seamlessly integrated directly **inside Match Setup (`BotSetupScreen`) and Color Selection (`ColorSelectionScreen`)** as requested.
- **7-Day Consecutive Daily Login System**:
  - Day 1: 10 coins.
  - Days 2–6: Scaling by 1.25x daily (Day 2 = 12, Day 3 = 15, Day 4 = 19, Day 5 = 24, Day 6 = 30).
  - Day 7 Mystery Box: Generous randomized rewards (*Unlimited Both* [35%], *Unlimited Themes* [25%], *Unlimited Colors* [20%], *100 Coins* [15%], *Better Luck Next Time* [5%]).
  - Missed Day Recovery: Watch a rewarded ad to preserve the streak instead of resetting to Day 1.
  - Live 24h countdown timer until next claim, claim animations, and persistent state.

---

## 2. Architecture & Data Models

### 2.1 Models (`app/src/main/java/com/example/ludo/model/`)

1. **`PawnColorCatalog.kt`**:
   - `PawnColorSkin`: `id`, `displayName`, `primaryColor`, `surfaceColor`, `accentColor`, `darkShade`, `priceCoins` (25 for premium, 0 for classic), `isDefaultUnlocked`.
   - 4 Defaults: Classic Red, Green, Yellow, Blue.
   - 6 Premium:
     - **Ruby Royal** (`#E11D48`): Lustrous crimson with gold trim.
     - **Neon Cyan** (`#06B6D4`): Vibrant glowing electric cyan.
     - **Emerald Jade** (`#059669`): Deep imperial jade green.
     - **Imperial Amethyst** (`#9333EA`): Majestic royal violet.
     - **Rose Quartz** (`#F43F5E`): Soft pastel pinkish coral blush.
     - **Amber Sunset** (`#EA580C`): Radiant warm fiery orange.

2. **`BoardThemeCatalog.kt`**:
   - `BoardTheme`: `id`, `displayName`, `priceCoins` (150 for premium, 0 for default), `boardBackground`, `gridBorder`, `gridCell`, `alternateCell`, `starColor`, `centerGradientColors`, `description`.
   - 5 Exclusive Themes:
     - **Royal Navy & Brass Gold** (Default, Unlocked)
     - **Royal Gold & Ivory** (150 Coins): Opulent cream and burnished brass finish.
     - **Midnight Obsidian** (150 Coins): Stealth matte obsidian slate with sleek neon lines.
     - **Cyberpunk Neon** (150 Coins): Futuristic synthwave grid with electric magenta & cyan stars.
     - **Classic Wooden Parchment** (150 Coins): Warm mahogany grain, vintage brass studs, and parchment cells.
     - **Nebula Galaxy** (150 Coins): Cosmic deep violet cosmos with celestial starlight hues.

3. **`DailyRewardModels.kt`**:
   - `DailyRewardState`: `currentStreakDay` (1..7), `lastClaimTimestampMs`, `canClaimToday`, `isStreakAtRisk`, `timeRemainingMs`.
   - `MysteryBoxRewardType`: `UNLIMITED_BOTH`, `UNLIMITED_THEMES`, `UNLIMITED_COLORS`, `BONUS_100_COINS`, `BETTER_LUCK`.
   - Probability engine with user's selected generous weighting.

### 2.2 Persistence in `GamePreferences.kt`

Add persistent properties and atomic mutating functions:
- `coinsBalance`: Read/write integer (defaults to 50 for new players to start testing).
- `unlockedPawnColorIds`: Set of string IDs (always includes the 4 default colors).
- `unlockedBoardThemeIds`: Set of string IDs (always includes the default theme).
- `selectedBoardThemeId`: Currently equipped board theme.
- `hasUnlimitedPawnColorsPass`: Boolean flag (from Mystery Box).
- `hasUnlimitedBoardThemesPass`: Boolean flag (from Mystery Box).
- `dailyLoginStreakDay`: 1 to 7.
- `lastDailyClaimTimestamp`: Unix timestamp of the last claim.
- `addCoins(amount: Int)`: Validates amount > 0 and writes to disk.
- `spendCoins(amount: Int): Boolean`: Validates balance >= amount, deducts, and returns success.
- `unlockPawnColor(colorId: String): Boolean`: Validates coins and unlocks.
- `unlockBoardTheme(themeId: String): Boolean`: Validates coins and unlocks.
- `claimDailyReward(): DailyClaimResult`: Atomically verifies eligibility, advances streak, rolls Day 7 Mystery Box if applicable, and writes timestamp.
- `recoverStreakWithAd(): Boolean`: Restores at-risk streak after verified rewarded ad.

---

## 3. UI Implementation Details

### 3.1 Color & Setup Screen Customization (`ColorSelectionScreen.kt` & `BotSetupScreen.kt`)
- **Top Coin Bar Badge**: Displays current coins (`500 🪙`) with an attractive `+ Free Coins (50)` chip that launches Google AdMob's Rewarded Ad.
- **Theme Selection Carousel / Strip**: Preview cards showing the board themes with a lock badge, 150 Coins price tag, or "Equipped / Tap to Equip" state.
- **Pawn Color Customizer**:
  - Horizontal selector showing both the 4 classic colors and 6 premium colors.
  - Locked colors display a 25 Coin price badge; tapping prompts a quick, tactile confirmation dialog ("Unlock Ruby Royal for 25 Coins?").
  - Unlocked colors equip instantly with haptic feedback.

### 3.2 Dynamic Board Theming (`LudoBoardView.kt`)
- Wire `LudoBoardView` to accept `activeTheme: BoardTheme`.
- Render board background, corner yards, perimeter tiles, and central victory star using the active theme's color palette and brushes.

### 3.3 Daily Login Rewards UI (`DailyRewardDialog.kt` & Home Screen Integration)
- **Home Screen Entry**: Add a sleek "Daily Reward" calendar icon chip in the header with a glowing notification badge when a claim or streak recovery is pending.
- **Daily Rewards Dialog / Modal**:
  - 7 cards displayed in a clean grid/row: Day 1 (10), Day 2 (12), Day 3 (15), Day 4 (19), Day 5 (24), Day 6 (30), and Day 7 (Mystery Box).
  - Claimed days show checkmarks with dimmed styling.
  - Current day glows with a pulsing gold border.
  - Future days are locked with padlock icons.
  - "Claim Today" button with tactile click, coin particle sound, and celebratory dialog.
  - Live countdown timer ("Next reward in 14h 22m") when already claimed.
  - **Streak Recovery Flow**: If `isStreakAtRisk`, displays a banner: "Streak At Risk! Watch a video to preserve Day X streak, or restart from Day 1" with a direct "Watch Video to Recover" button.
  - **Mystery Box Open Modal**: Exciting tap-to-open box animation that bursts open into the randomized reward card with celebratory sound.

---

## 4. Verification & Testing

- **Robolectric & Unit Tests**:
  - `GamePreferencesEconomyTest.kt`: Tests coin addition, deduction, color unlocking, theme unlocking, and insufficient funds prevention.
  - `DailyRewardLogicTest.kt`: Tests the 1.25x scaling math (10, 12, 15, 19, 24, 30), Day 7 reset, streak expiration, and Mystery Box odds distribution.
- **Compilation Check**:
  - Run `compile_applet` to ensure zero compilation or syntax errors.
  - Run `gradle :app:testDebugUnitTest` to guarantee all tests pass.
- **End-to-End User Verification**:
  - Verify watching a rewarded ad awards exactly 50 coins only upon completion.
  - Verify purchasing pawn colors (25 coins) and board themes (150 coins) persists across app restarts.
  - Verify Day 7 Mystery Box grants the randomized rewards and unlocks properly.
