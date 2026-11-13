package com.example.ludo.p2p

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * High-speed Wi-Fi Direct / Local Hotspot / LAN peer-to-peer transport using
 * Android Network Service Discovery (NsdManager) and multi-client TCP Sockets.
 * Supports direct connection for 2-4 nearby phones without an external server.
 */
class WifiP2PTransport(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPacketReceived: (packet: P2PPacket, senderPeerId: String) -> Unit,
    private val onPeerConnected: (peerId: String) -> Unit,
    private val onPeerDisconnected: (peerId: String) -> Unit,
    private val onRoomDiscovered: (room: DiscoveredRoom) -> Unit,
    private val onRoomLost: (roomId: String) -> Unit,
    private val onError: (message: String) -> Unit
) {
    companion object {
        private const val TAG = "WifiP2PTransport"
        private const val SERVICE_TYPE = "_ludomulti._tcp."
        const val DEFAULT_PORT = 8988
    }

    private val nsdManager: NsdManager? by lazy {
        context.getSystemService(Context.NSD_SERVICE) as? NsdManager
    }

    private var multicastLock: WifiManager.MulticastLock? = null
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var clientWriter: PrintWriter? = null

    // For Host: Map peerId -> ConnectedClient
    private val connectedClients = ConcurrentHashMap<String, ConnectedClient>()

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var serverAcceptJob: Job? = null
    private var clientReadJob: Job? = null

    var currentPort: Int = DEFAULT_PORT
        private set
    var currentRoomCode: String = "0000"
        private set
    var localIpAddress: String = "127.0.0.1"
        private set

    init {
        try {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
            multicastLock = wifiManager?.createMulticastLock("LudoMulticastLock")?.apply {
                setReferenceCounted(true)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to create multicast lock: ${e.message}")
        }
    }

    private data class ConnectedClient(
        val peerId: String,
        val socket: Socket,
        val writer: PrintWriter,
        val job: Job
    )

    fun getDeviceLocalIpAddress(): String {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    // ----------------------------------------------------
    // HOST MODE
    // ----------------------------------------------------

    fun startHosting(roomName: String, maxPlayers: Int, hostPlayerName: String): Boolean {
        stopAll()
        localIpAddress = getDeviceLocalIpAddress()

        try {
            multicastLock?.acquire()
        } catch (_: Exception) {}

        // Bind ServerSocket
        var port = DEFAULT_PORT
        var bound = false
        for (p in DEFAULT_PORT..DEFAULT_PORT + 10) {
            try {
                serverSocket = ServerSocket(p)
                port = p
                bound = true
                break
            } catch (_: Exception) {}
        }

        if (!bound || serverSocket == null) {
            onError("Could not bind game server socket on port $DEFAULT_PORT")
            return false
        }

        currentPort = port
        // Generate memorable 4-digit room code from IP/Port
        val ipParts = localIpAddress.split(".")
        currentRoomCode = if (ipParts.size >= 4) {
            val part3 = ipParts[2].toIntOrNull() ?: 1
            val part4 = ipParts[3].toIntOrNull() ?: 1
            String.format("%02d%02d", (part3 % 100), (part4 % 100))
        } else {
            (1000..9999).random().toString()
        }

        // Accept incoming client sockets
        serverAcceptJob = coroutineScope.launch(Dispatchers.IO) {
            val srv = serverSocket ?: return@launch
            while (isActive && !srv.isClosed) {
                try {
                    val socket = srv.accept()
                    val peerId = "peer_${socket.inetAddress.hostAddress}_${System.currentTimeMillis() % 10000}"
                    val writer = PrintWriter(socket.getOutputStream(), true)
                    val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                    val readJob = launch {
                        try {
                            while (isActive && socket.isConnected && !socket.isClosed) {
                                val line = reader.readLine() ?: break
                                val packet = P2PProtocol.decodePacket(line)
                                if (packet != null) {
                                    withContext(Dispatchers.Main) {
                                        onPacketReceived(packet, peerId)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Client read error: ${e.message}")
                        } finally {
                            withContext(Dispatchers.Main) {
                                removeClient(peerId)
                            }
                        }
                    }

                    val client = ConnectedClient(peerId, socket, writer, readJob)
                    connectedClients[peerId] = client

                    withContext(Dispatchers.Main) {
                        onPeerConnected(peerId)
                    }
                } catch (e: Exception) {
                    if (isActive) {
                        Log.d(TAG, "ServerSocket accept exception: ${e.message}")
                    }
                }
            }
        }

        // Register NSD service
        registerNsdService(roomName, hostPlayerName, port, maxPlayers)
        return true
    }

    private fun registerNsdService(roomName: String, hostPlayerName: String, port: Int, maxPlayers: Int) {
        val sanitizedRoomName = roomName.replace(" ", "_").take(16)
        val serviceInfo = NsdServiceInfo().apply {
            serviceType = SERVICE_TYPE
            serviceName = "Ludo_${currentRoomCode}_$sanitizedRoomName"
            setPort(port)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setAttribute("host", hostPlayerName)
                setAttribute("max", maxPlayers.toString())
                setAttribute("code", currentRoomCode)
                setAttribute("ip", localIpAddress)
            }
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(NsdServiceInfo: NsdServiceInfo) {
                Log.i(TAG, "NSD Service registered: ${NsdServiceInfo.serviceName}")
            }
            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD Registration failed: $errorCode")
            }
            override fun onServiceUnregistered(arg0: NsdServiceInfo) {
                Log.i(TAG, "NSD Service unregistered")
            }
            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "NSD Unregistration failed: $errorCode")
            }
        }

        try {
            nsdManager?.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error registering NSD: ${e.message}")
        }
    }

    private fun removeClient(peerId: String) {
        val client = connectedClients.remove(peerId)
        client?.job?.cancel()
        try {
            client?.socket?.close()
        } catch (_: Exception) {}
        onPeerDisconnected(peerId)
    }

    // ----------------------------------------------------
    // CLIENT DISCOVERY & CONNECTION
    // ----------------------------------------------------

    fun startDiscovery() {
        stopDiscovery()
        try {
            multicastLock?.acquire()
        } catch (_: Exception) {}

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                Log.i(TAG, "Service discovery started")
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                Log.i(TAG, "Service found: ${service.serviceName}")
                if (service.serviceType.contains(SERVICE_TYPE.replace(".", "")) ||
                    service.serviceType == SERVICE_TYPE) {
                    resolveService(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                Log.i(TAG, "Service lost: ${service.serviceName}")
                coroutineScope.launch(Dispatchers.Main) {
                    onRoomLost(service.serviceName)
                }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                Log.i(TAG, "Discovery stopped: $serviceType")
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Discovery failed: Error code: $errorCode")
                stopDiscovery()
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                Log.e(TAG, "Stop discovery failed: Error code: $errorCode")
            }
        }

        try {
            nsdManager?.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting discovery: ${e.message}")
        }
    }

    private fun resolveService(serviceInfo: NsdServiceInfo) {
        nsdManager?.resolveService(serviceInfo, object : NsdManager.ResolveListener {
            override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                Log.e(TAG, "Resolve failed: $errorCode")
            }

            override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                val host = serviceInfo.host ?: return
                val port = serviceInfo.port
                val hostIp = host.hostAddress ?: return

                var roomCode = ""
                var maxPlayers = 4
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val attrs = serviceInfo.attributes
                    roomCode = attrs["code"]?.let { String(it) } ?: ""
                    maxPlayers = attrs["max"]?.let { String(it).toIntOrNull() } ?: 4
                }

                val displayName = serviceInfo.serviceName
                    .replace("Ludo_", "")
                    .replace("_", " ")

                val room = DiscoveredRoom(
                    id = serviceInfo.serviceName,
                    name = displayName,
                    hostAddress = hostIp,
                    port = port,
                    currentPlayers = 1,
                    maxPlayers = maxPlayers,
                    transport = P2PTransportType.WIFI_DIRECT_LAN
                )

                coroutineScope.launch(Dispatchers.Main) {
                    onRoomDiscovered(room)
                }
            }
        })
    }

    fun stopDiscovery() {
        discoveryListener?.let {
            try {
                nsdManager?.stopServiceDiscovery(it)
            } catch (_: Exception) {}
        }
        discoveryListener = null
    }

    fun connectToHost(hostAddress: String, port: Int) {
        stopClientConnection()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val socket = Socket(hostAddress, port)
                socket.tcpNoDelay = true
                clientSocket = socket
                val writer = PrintWriter(socket.getOutputStream(), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                clientWriter = writer

                withContext(Dispatchers.Main) {
                    onPeerConnected("host")
                }

                clientReadJob = launch {
                    try {
                        while (isActive && socket.isConnected && !socket.isClosed) {
                            val line = reader.readLine() ?: break
                            val packet = P2PProtocol.decodePacket(line)
                            if (packet != null) {
                                withContext(Dispatchers.Main) {
                                    onPacketReceived(packet, "host")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "Client read error: ${e.message}")
                    } finally {
                        withContext(Dispatchers.Main) {
                            onPeerDisconnected("host")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Failed to connect to host: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }

    // ----------------------------------------------------
    // PACKET SENDING (BROADCAST / DIRECT)
    // ----------------------------------------------------

    fun sendPacketToAll(packet: P2PPacket) {
        val payload = P2PProtocol.encodePacket(packet)
        coroutineScope.launch(Dispatchers.IO) {
            // If we are Host: send to all connected clients
            for (client in connectedClients.values) {
                try {
                    client.writer.println(payload)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed sending to client ${client.peerId}: ${e.message}")
                }
            }
            // If we are Client: send to host
            clientWriter?.let { writer ->
                try {
                    writer.println(payload)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed sending to host: ${e.message}")
                }
            }
        }
    }

    fun sendPacketToPeer(peerId: String, packet: P2PPacket) {
        val payload = P2PProtocol.encodePacket(packet)
        coroutineScope.launch(Dispatchers.IO) {
            if (peerId == "host") {
                clientWriter?.println(payload)
            } else {
                connectedClients[peerId]?.writer?.println(payload)
            }
        }
    }

    // ----------------------------------------------------
    // CLEANUP
    // ----------------------------------------------------

    fun stopAll() {
        stopDiscovery()

        registrationListener?.let {
            try {
                nsdManager?.unregisterService(it)
            } catch (_: Exception) {}
        }
        registrationListener = null

        serverAcceptJob?.cancel()
        serverAcceptJob = null

        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null

        for (c in connectedClients.values) {
            c.job.cancel()
            try {
                c.socket.close()
            } catch (_: Exception) {}
        }
        connectedClients.clear()

        stopClientConnection()

        try {
            if (multicastLock?.isHeld == true) {
                multicastLock?.release()
            }
        } catch (_: Exception) {}
    }

    private fun stopClientConnection() {
        clientReadJob?.cancel()
        clientReadJob = null
        try {
            clientSocket?.close()
        } catch (_: Exception) {}
        clientSocket = null
        clientWriter = null
    }
}
