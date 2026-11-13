package com.example.ludo.ui.screens

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ludo.data.GamePreferences
import com.example.ludo.model.PlayerColor
import com.example.ludo.p2p.DiscoveredRoom
import com.example.ludo.p2p.P2PConnectionState
import com.example.ludo.p2p.P2PGameManager
import com.example.ludo.p2p.P2PLobbyState
import com.example.ludo.p2p.P2PPlayerSlot
import com.example.ludo.p2p.P2PRole
import com.example.ludo.p2p.P2PTransportType
import com.example.ludo.ui.components.GameBackground
import com.example.ludo.ui.components.GameButton
import com.example.ludo.ui.components.GameButtonStyle
import com.example.ui.theme.AccentPrimary
import com.example.ui.theme.BorderHairline
import com.example.ui.theme.StarGold
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.Typography

enum class MultiplayerTab {
    HOST,
    JOIN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyMultiplayerScreen(
    gamePreferences: GamePreferences,
    onBackClick: () -> Unit,
    onGameStarted: () -> Unit
) {
    val context = LocalContext.current
    val p2pManager = remember { P2PGameManager.getInstance(context) }

    val connectionState by p2pManager.connectionState.collectAsState()
    val transportType by p2pManager.transportType.collectAsState()
    val role by p2pManager.role.collectAsState()
    val lobbyState by p2pManager.lobbyState.collectAsState()
    val discoveredRooms by p2pManager.discoveredRooms.collectAsState()
    val discoveredBtDevices by p2pManager.discoveredBtDevices.collectAsState()
    val errorMessage by p2pManager.errorMessage.collectAsState()
    val localSlotIdx by p2pManager.localPlayerIndex.collectAsState()

    var activeTab by rememberSaveable { mutableStateOf(MultiplayerTab.HOST) }
    var selectedTransport by rememberSaveable { mutableStateOf(P2PTransportType.WIFI_DIRECT_LAN) }
    var selectedPlayerCount by rememberSaveable { mutableIntStateOf(4) }

    val defaultHostName = remember { gamePreferences.getPlayerName(0, "Player 1") }
    var hostRoomName by rememberSaveable { mutableStateOf("${defaultHostName}'s Arena") }
    var hostPlayerName by rememberSaveable { mutableStateOf(defaultHostName) }
    var clientPlayerName by rememberSaveable { mutableStateOf(defaultHostName) }
    var manualRoomCodeOrIp by rememberSaveable { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Permission launcher for Bluetooth
    val permissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val allGranted = grants.values.all { it }
        if (allGranted && activeTab == MultiplayerTab.JOIN) {
            p2pManager.startDiscovery(selectedTransport)
        }
    }

    // Trigger game start navigation when connection enters IN_GAME
    LaunchedEffect(connectionState) {
        if (connectionState == P2PConnectionState.IN_GAME) {
            onGameStarted()
        }
    }

    // Lifecycle clean up on back
    BackHandler {
        if (connectionState == P2PConnectionState.CONNECTED_LOBBY) {
            p2pManager.leaveGame()
        } else {
            p2pManager.leaveGame()
            onBackClick()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (connectionState != P2PConnectionState.IN_GAME) {
                p2pManager.leaveGame()
            }
        }
    }

    fun checkAndRequestPermissions(onGranted: () -> Unit) {
        if (selectedTransport == P2PTransportType.BLUETOOTH) {
            val neededPermissions = mutableListOf<String>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
                }
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
            if (neededPermissions.isNotEmpty()) {
                permissionsLauncher.launch(neededPermissions.toTypedArray())
            } else {
                onGranted()
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
                    permissionsLauncher.launch(arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES))
                    return
                }
            }
            onGranted()
        }
    }

    GameBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("nearby_multiplayer_screen"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard)
                                .border(1.dp, BorderHairline, CircleShape)
                                .clickable {
                                    if (connectionState == P2PConnectionState.CONNECTED_LOBBY) {
                                        p2pManager.leaveGame()
                                    } else {
                                        onBackClick()
                                    }
                                }
                                .testTag("p2p_back_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Nearby Multiplayer",
                                style = Typography.titleLarge.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Local Wi-Fi & Bluetooth Direct (2-4 Phones)",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = AccentPrimary
                            )
                        }
                    }

                    // Transport Indicator Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(SurfaceElevated)
                            .border(1.dp, BorderHairline, RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (selectedTransport == P2PTransportType.WIFI_DIRECT_LAN) Icons.Default.Wifi else Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = AccentPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (selectedTransport == P2PTransportType.WIFI_DIRECT_LAN) "Wi-Fi P2P" else "Bluetooth",
                                style = Typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = TextPrimary
                            )
                        }
                    }
                }

                // Error Banner
                AnimatedVisibility(visible = errorMessage != null) {
                    errorMessage?.let { err ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(StatusDanger.copy(alpha = 0.15f))
                                .border(1.dp, StatusDanger.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = err,
                                style = Typography.bodySmall.copy(fontSize = 12.sp),
                                color = StatusDanger,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { p2pManager.clearError() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = StatusDanger,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Body content based on connection state
                when (connectionState) {
                    P2PConnectionState.CONNECTED_LOBBY -> {
                        lobbyState?.let { lobby ->
                            LobbyScreenContent(
                                lobby = lobby,
                                role = role,
                                localSlotIdx = localSlotIdx,
                                onToggleBot = { slotIdx -> p2pManager.toggleSlotBot(slotIdx) },
                                onToggleReady = { p2pManager.toggleClientReady() },
                                onStartMatch = { p2pManager.startMatch() },
                                onLeaveLobby = { p2pManager.leaveGame() },
                                onSendEmote = { emote -> p2pManager.sendQuickEmote(emote) },
                                canStart = p2pManager.canStartMatch()
                            )
                        }
                    }

                    P2PConnectionState.CONNECTING -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = AccentPrimary,
                                    modifier = Modifier.size(54.dp),
                                    strokeWidth = 3.dp
                                )
                                Text(
                                    text = "Connecting to Host...",
                                    style = Typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Negotiating peer-to-peer room slot...",
                                    style = Typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                GameButton(
                                    text = "Cancel Connection",
                                    style = GameButtonStyle.SECONDARY_GLASS,
                                    onClick = { p2pManager.leaveGame() }
                                )
                            }
                        }
                    }

                    else -> {
                        // Setup / Discovery Tabs
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Transport Type Selector
                            TransportSelector(
                                selectedTransport = selectedTransport,
                                onTransportSelected = { newTransport ->
                                    selectedTransport = newTransport
                                    p2pManager.stopDiscovery()
                                }
                            )

                            // Mode Tabs: [HOST MATCH] & [JOIN MATCH]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (activeTab == MultiplayerTab.HOST) AccentPrimary else Color.Transparent)
                                        .clickable {
                                            activeTab = MultiplayerTab.HOST
                                            p2pManager.stopDiscovery()
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("tab_host_game"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "HOST MATCH",
                                        style = Typography.titleSmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = if (activeTab == MultiplayerTab.HOST) Color(0xFF0A0F1D) else TextSecondary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(if (activeTab == MultiplayerTab.JOIN) AccentPrimary else Color.Transparent)
                                        .clickable {
                                            activeTab = MultiplayerTab.JOIN
                                            checkAndRequestPermissions {
                                                p2pManager.startDiscovery(selectedTransport)
                                            }
                                        }
                                        .padding(vertical = 10.dp)
                                        .testTag("tab_join_game"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "JOIN MATCH",
                                        style = Typography.titleSmall.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.6.sp
                                        ),
                                        color = if (activeTab == MultiplayerTab.JOIN) Color(0xFF0A0F1D) else TextSecondary
                                    )
                                }
                            }

                            if (activeTab == MultiplayerTab.HOST) {
                                HostMatchForm(
                                    transport = selectedTransport,
                                    roomName = hostRoomName,
                                    onRoomNameChange = { hostRoomName = it },
                                    hostPlayerName = hostPlayerName,
                                    onHostPlayerNameChange = { hostPlayerName = it },
                                    playerCount = selectedPlayerCount,
                                    onPlayerCountChange = { selectedPlayerCount = it },
                                    onHostClick = {
                                        checkAndRequestPermissions {
                                            val ok = p2pManager.hostRoom(
                                                transport = selectedTransport,
                                                roomName = hostRoomName.ifBlank { "Ludo Room" },
                                                maxPlayers = selectedPlayerCount,
                                                hostName = hostPlayerName.ifBlank { "Player 1" }
                                            )
                                            if (ok) {
                                                gamePreferences.setPlayerName(0, hostPlayerName)
                                            }
                                        }
                                    }
                                )
                            } else {
                                JoinMatchView(
                                    transport = selectedTransport,
                                    clientPlayerName = clientPlayerName,
                                    onClientPlayerNameChange = { clientPlayerName = it },
                                    isScanning = connectionState == P2PConnectionState.DISCOVERING,
                                    discoveredRooms = discoveredRooms,
                                    discoveredBtDevices = discoveredBtDevices,
                                    manualCodeOrIp = manualRoomCodeOrIp,
                                    onManualCodeChange = { manualRoomCodeOrIp = it },
                                    onStartScanClick = {
                                        checkAndRequestPermissions {
                                            p2pManager.startDiscovery(selectedTransport)
                                        }
                                    },
                                    onJoinRoom = { room ->
                                        p2pManager.joinRoom(room, clientPlayerName.ifBlank { "Player 2" })
                                    },
                                    onJoinBtDevice = { device ->
                                        p2pManager.joinBluetoothDevice(device, clientPlayerName.ifBlank { "Player 2" })
                                    },
                                    onJoinDirect = { target ->
                                        val trimmed = target.trim()
                                        if (trimmed.contains(".")) {
                                            p2pManager.joinByIp(trimmed, playerName = clientPlayerName.ifBlank { "Player 2" })
                                        } else {
                                            val room = discoveredRooms.firstOrNull { it.name.contains(trimmed, ignoreCase = true) }
                                            if (room != null) {
                                                p2pManager.joinRoom(room, clientPlayerName.ifBlank { "Player 2" })
                                            } else {
                                                p2pManager.joinByIp(trimmed, playerName = clientPlayerName.ifBlank { "Player 2" })
                                            }
                                        }
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TransportSelector(
    selectedTransport: P2PTransportType,
    onTransportSelected: (P2PTransportType) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "CONNECTION PROTOCOL",
            style = Typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = StarGold
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TransportOptionCard(
                modifier = Modifier.weight(1f),
                title = "Wi-Fi / Hotspot",
                subtitle = "Auto-discover on same Wi-Fi or Personal Hotspot",
                icon = Icons.Default.Wifi,
                isSelected = selectedTransport == P2PTransportType.WIFI_DIRECT_LAN,
                onClick = { onTransportSelected(P2PTransportType.WIFI_DIRECT_LAN) },
                testTag = "transport_wifi"
            )

            TransportOptionCard(
                modifier = Modifier.weight(1f),
                title = "Bluetooth P2P",
                subtitle = "Direct device pairing without any Wi-Fi needed",
                icon = Icons.Default.Bluetooth,
                isSelected = selectedTransport == P2PTransportType.BLUETOOTH,
                onClick = { onTransportSelected(P2PTransportType.BLUETOOTH) },
                testTag = "transport_bluetooth"
            )
        }
    }
}

@Composable
private fun TransportOptionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) AccentPrimary.copy(alpha = 0.12f) else SurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentPrimary else BorderHairline,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) AccentPrimary else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Selected",
                        tint = AccentPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Text(
                text = title,
                style = Typography.titleSmall.copy(fontSize = 13.sp, fontWeight = FontWeight.Bold),
                color = if (isSelected) AccentPrimary else TextPrimary
            )
            Text(
                text = subtitle,
                style = Typography.bodySmall.copy(fontSize = 10.sp),
                color = TextMuted,
                lineHeight = 13.sp
            )
        }
    }
}

@Composable
private fun HostMatchForm(
    transport: P2PTransportType,
    roomName: String,
    onRoomNameChange: (String) -> Unit,
    hostPlayerName: String,
    onHostPlayerNameChange: (String) -> Unit,
    playerCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onHostClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "HOST A NEW GAME ROOM",
            style = Typography.titleMedium.copy(fontSize = 15.sp, fontWeight = FontWeight.Bold),
            color = TextPrimary
        )

        // Player Name
        OutlinedTextField(
            value = hostPlayerName,
            onValueChange = onHostPlayerNameChange,
            label = { Text("Your Player Name", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("host_player_name_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = BorderHairline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Room Name
        OutlinedTextField(
            value = roomName,
            onValueChange = onRoomNameChange,
            label = { Text("Room Name", color = TextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("host_room_name_field"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPrimary,
                unfocusedBorderColor = BorderHairline,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Player Count Selector
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "MATCH CAPACITY",
                style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(2, 3, 4).forEach { count ->
                    val isSelected = playerCount == count
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) AccentPrimary.copy(alpha = 0.2f) else SurfaceElevated)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentPrimary else BorderHairline,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onPlayerCountChange(count) }
                            .padding(vertical = 10.dp)
                            .testTag("player_count_$count"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$count Players",
                            style = Typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) AccentPrimary else TextPrimary
                        )
                    }
                }
            }
        }

        // Info Banner
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceElevated)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = AccentPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
                    "Nearby phones on the same Wi-Fi or connected to your Personal Hotspot will see this room instantly."
                } else {
                    "Nearby phones will connect directly via Bluetooth. Make sure Bluetooth is enabled on all devices."
                },
                style = Typography.bodySmall.copy(fontSize = 11.sp),
                color = TextSecondary
            )
        }

        GameButton(
            text = "Create Room & Open Lobby",
            icon = Icons.Default.PlayArrow,
            style = GameButtonStyle.PRIMARY_GOLD,
            onClick = onHostClick,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("btn_create_room")
        )
    }
}

@Composable
private fun JoinMatchView(
    transport: P2PTransportType,
    clientPlayerName: String,
    onClientPlayerNameChange: (String) -> Unit,
    isScanning: Boolean,
    discoveredRooms: List<DiscoveredRoom>,
    discoveredBtDevices: List<BluetoothDevice>,
    manualCodeOrIp: String,
    onManualCodeChange: (String) -> Unit,
    onStartScanClick: () -> Unit,
    onJoinRoom: (DiscoveredRoom) -> Unit,
    onJoinBtDevice: (BluetoothDevice) -> Unit,
    onJoinDirect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Player Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = clientPlayerName,
                onValueChange = onClientPlayerNameChange,
                label = { Text("Your Player Name", color = TextSecondary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("client_player_name_field"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentPrimary,
                    unfocusedBorderColor = BorderHairline,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )
        }

        // Direct Code or IP Join Card (Fast join)
        if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DIRECT QUICK JOIN",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = StarGold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualCodeOrIp,
                        onValueChange = onManualCodeChange,
                        placeholder = { Text("4-digit Code or Host IP (e.g. 192.168.1.5)", fontSize = 12.sp, color = TextMuted) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_code_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AccentPrimary,
                            unfocusedBorderColor = BorderHairline,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentPrimary)
                            .clickable {
                                if (manualCodeOrIp.isNotBlank()) {
                                    onJoinDirect(manualCodeOrIp)
                                }
                            }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                            .testTag("btn_direct_connect"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Connect",
                            style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF090E1A)
                        )
                    }
                }
            }
        }

        // Discovered Rooms / Devices List
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderHairline, RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (transport == P2PTransportType.WIFI_DIRECT_LAN) "NEARBY WI-FI ROOMS" else "BLUETOOTH DEVICES",
                        style = Typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        ),
                        color = StarGold
                    )
                    if (isScanning) {
                        Spacer(modifier = Modifier.width(8.dp))
                        CircularProgressIndicator(
                            color = AccentPrimary,
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .clickable { onStartScanClick() }
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                        .testTag("btn_refresh_scan")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Scan",
                            tint = AccentPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Scan",
                            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                            color = TextPrimary
                        )
                    }
                }
            }

            if (transport == P2PTransportType.WIFI_DIRECT_LAN) {
                if (discoveredRooms.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiTethering,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = if (isScanning) "Searching for nearby Ludo rooms..." else "No rooms detected yet",
                                style = Typography.bodyMedium.copy(fontSize = 13.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = "Ensure the host has created a room on this Wi-Fi / Hotspot",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    discoveredRooms.forEach { room ->
                        DiscoveredRoomCard(
                            room = room,
                            onJoin = { onJoinRoom(room) }
                        )
                    }
                }
            } else {
                // Bluetooth Devices
                if (discoveredBtDevices.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bluetooth,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "No paired Bluetooth devices found",
                                style = Typography.bodyMedium.copy(fontSize = 13.sp),
                                color = TextSecondary
                            )
                            Text(
                                text = "Pair devices in Android Settings or tap Scan to find host",
                                style = Typography.bodySmall.copy(fontSize = 11.sp),
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    discoveredBtDevices.forEach { dev ->
                        BluetoothDeviceCard(
                            device = dev,
                            onConnect = { onJoinBtDevice(dev) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiscoveredRoomCard(
    room: DiscoveredRoom,
    onJoin: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onJoin)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = room.name,
                    style = Typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "IP: ${room.hostAddress} • Max ${room.maxPlayers} Players",
                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPrimary)
                .clickable { onJoin() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("btn_join_room_${room.id}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Join",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF090E1A)
            )
        }
    }
}

@Composable
private fun BluetoothDeviceCard(
    device: BluetoothDevice,
    onConnect: () -> Unit
) {
    val devName = try {
        device.name ?: device.address
    } catch (_: SecurityException) {
        device.address
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceElevated)
            .border(1.dp, BorderHairline, RoundedCornerShape(12.dp))
            .clickable(onClick = onConnect)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AccentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bluetooth,
                    contentDescription = null,
                    tint = AccentPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = devName,
                    style = Typography.titleSmall.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = device.address,
                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextMuted
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(AccentPrimary)
                .clickable { onConnect() }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("btn_connect_bt_${device.address}"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Connect",
                style = Typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF090E1A)
            )
        }
    }
}

@Composable
private fun LobbyScreenContent(
    lobby: P2PLobbyState,
    role: P2PRole,
    localSlotIdx: Int,
    onToggleBot: (Int) -> Unit,
    onToggleReady: () -> Unit,
    onStartMatch: () -> Unit,
    onLeaveLobby: () -> Unit,
    onSendEmote: (String) -> Unit,
    canStart: Boolean
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Room Header Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceCard)
                .border(1.2.dp, StarGold.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "LUDO MATCH LOBBY",
                    style = Typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    ),
                    color = StarGold
                )
                Text(
                    text = lobby.roomName,
                    style = Typography.titleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "Host: ${lobby.hostName} • ${lobby.slots.count { it.isConnected }} / ${lobby.maxPlayers} Players",
                    style = Typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }

            // Room Quick Code Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF19140A))
                    .border(1.dp, StarGold.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "ROOM CODE",
                        style = Typography.labelSmall.copy(fontSize = 8.sp, fontWeight = FontWeight.Bold),
                        color = StarGold
                    )
                    Text(
                        text = lobby.roomCode,
                        style = Typography.titleMedium.copy(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = StarGold
                    )
                }
            }
        }

        // Player Slots List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            lobby.slots.forEach { slot ->
                PlayerSlotCard(
                    slot = slot,
                    isLocalPlayer = slot.playerIndex == localSlotIdx,
                    isHostViewer = role == P2PRole.HOST,
                    onToggleBot = { onToggleBot(slot.playerIndex) }
                )
            }
        }

        // Quick Emotes strip in Lobby
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("👋 Hello!", "🎲 Ready!", "🔥 Let's Roll!", "👑 GG!").forEach { emote ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                        .clickable { onSendEmote(emote) }
                        .padding(vertical = 7.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emote,
                        style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = TextPrimary
                    )
                }
            }
        }

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (role == P2PRole.HOST) {
                GameButton(
                    text = if (canStart) "Start Match Now!" else "Waiting for Players to Ready Up...",
                    icon = Icons.Default.PlayArrow,
                    style = if (canStart) GameButtonStyle.PRIMARY_GOLD else GameButtonStyle.SECONDARY_GLASS,
                    onClick = onStartMatch,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_host_start_game")
                )
            } else {
                val mySlot = lobby.slots.getOrNull(localSlotIdx)
                val isReady = mySlot?.isReady == true
                GameButton(
                    text = if (isReady) "You are Ready! (Tap to Cancel)" else "I'm Ready to Play!",
                    icon = if (isReady) Icons.Outlined.Check else Icons.Default.PlayArrow,
                    style = if (isReady) GameButtonStyle.SECONDARY_GLASS else GameButtonStyle.PRIMARY_GOLD,
                    onClick = onToggleReady,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_client_toggle_ready")
                )
            }

            GameButton(
                text = "Leave Lobby",
                style = GameButtonStyle.SECONDARY_GLASS,
                onClick = onLeaveLobby,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_leave_lobby")
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun PlayerSlotCard(
    slot: P2PPlayerSlot,
    isLocalPlayer: Boolean,
    isHostViewer: Boolean,
    onToggleBot: () -> Unit
) {
    val borderColor = when {
        isLocalPlayer -> AccentPrimary
        slot.isConnected -> slot.color.primaryColor.copy(alpha = 0.6f)
        else -> BorderHairline
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(if (isLocalPlayer) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Color Token / Avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(slot.color.primaryColor.copy(alpha = if (slot.isConnected) 0.9f else 0.25f))
                    .border(2.dp, slot.color.accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (slot.isBot) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "${slot.playerIndex + 1}",
                        style = Typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = slot.name,
                        style = Typography.titleSmall.copy(
                            fontSize = 14.sp,
                            fontWeight = if (isLocalPlayer) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (slot.isConnected) TextPrimary else TextMuted
                    )
                    if (isLocalPlayer) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(You)",
                            style = Typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                            color = AccentPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${slot.color.displayName} Team",
                        style = Typography.bodySmall.copy(fontSize = 11.sp),
                        color = slot.color.accentColor
                    )
                    if (slot.isHost) {
                        Text(text = "• Host", style = Typography.bodySmall.copy(fontSize = 11.sp), color = StarGold)
                    }
                }
            }
        }

        // Status Badge or Bot Toggle
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (slot.isConnected) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (slot.isReady) StatusSuccess.copy(alpha = 0.18f) else StatusWarning.copy(alpha = 0.18f))
                        .border(
                            1.dp,
                            if (slot.isReady) StatusSuccess.copy(alpha = 0.6f) else StatusWarning.copy(alpha = 0.6f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (slot.isReady) "READY" else "NOT READY",
                        style = Typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp
                        ),
                        color = if (slot.isReady) StatusSuccess else StatusWarning
                    )
                }
            }

            // If Host and this slot is not the host slot, allow toggling Bot
            if (isHostViewer && !slot.isHost) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceElevated)
                        .border(1.dp, BorderHairline, RoundedCornerShape(8.dp))
                        .clickable { onToggleBot() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("toggle_bot_slot_${slot.playerIndex}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = null,
                            tint = if (slot.isBot) StarGold else TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (slot.isBot) "Remove Bot" else "+ Add Bot",
                            style = Typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                            color = if (slot.isBot) StarGold else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
