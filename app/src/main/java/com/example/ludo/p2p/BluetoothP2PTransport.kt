package com.example.ludo.p2p

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Direct device-to-device Bluetooth RFCOMM peer-to-peer transport.
 * Allows 2-4 nearby players to connect without requiring Wi-Fi.
 */
class BluetoothP2PTransport(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val onPacketReceived: (packet: P2PPacket, senderPeerId: String) -> Unit,
    private val onPeerConnected: (peerId: String) -> Unit,
    private val onPeerDisconnected: (peerId: String) -> Unit,
    private val onDeviceDiscovered: (device: BluetoothDevice) -> Unit,
    private val onError: (message: String) -> Unit
) {
    companion object {
        private const val TAG = "BluetoothP2PTransport"
        val LUDO_BT_UUID: UUID = UUID.fromString("4a3b8c20-21a4-11ee-be56-0242ac120002")
    }

    private val bluetoothManager: BluetoothManager? by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    }

    val bluetoothAdapter: BluetoothAdapter? by lazy {
        bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()
    }

    private var serverSocket: BluetoothServerSocket? = null
    private var clientSocket: BluetoothSocket? = null
    private var clientWriter: PrintWriter? = null

    private val connectedClients = ConcurrentHashMap<String, ConnectedClient>()

    private var serverAcceptJob: Job? = null
    private var clientReadJob: Job? = null
    private var isReceiverRegistered = false

    private data class ConnectedClient(
        val peerId: String,
        val socket: BluetoothSocket,
        val writer: PrintWriter,
        val job: Job
    )

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    device?.let { onDeviceDiscovered(it) }
                }
            }
        }
    }

    fun isBluetoothAvailable(): Boolean = bluetoothAdapter != null
    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    @SuppressLint("MissingPermission")
    fun getPairedDevices(): List<BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get paired devices: ${e.message}")
            emptyList()
        }
    }

    // ----------------------------------------------------
    // HOST MODE
    // ----------------------------------------------------

    @SuppressLint("MissingPermission")
    fun startHosting(roomName: String): Boolean {
        stopAll()
        val adapter = bluetoothAdapter
        if (adapter == null || !adapter.isEnabled) {
            onError("Bluetooth is not enabled on this device")
            return false
        }

        try {
            serverSocket = adapter.listenUsingRfcommWithServiceRecord("LudoLegendsP2P", LUDO_BT_UUID)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create Bluetooth server socket: ${e.message}")
            onError("Cannot start Bluetooth host: ${e.localizedMessage}")
            return false
        }

        serverAcceptJob = coroutineScope.launch(Dispatchers.IO) {
            val srv = serverSocket ?: return@launch
            while (isActive) {
                try {
                    val socket = srv.accept() ?: break
                    val peerId = "bt_peer_${socket.remoteDevice?.address ?: System.currentTimeMillis()}"
                    val writer = PrintWriter(socket.outputStream, true)
                    val reader = BufferedReader(InputStreamReader(socket.inputStream))

                    val readJob = launch {
                        try {
                            while (isActive && socket.isConnected) {
                                val line = reader.readLine() ?: break
                                val packet = P2PProtocol.decodePacket(line)
                                if (packet != null) {
                                    withContext(Dispatchers.Main) {
                                        onPacketReceived(packet, peerId)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "BT client read error: ${e.message}")
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
                        Log.d(TAG, "BT ServerSocket accept ended: ${e.message}")
                    }
                    break
                }
            }
        }
        return true
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

    @SuppressLint("MissingPermission")
    fun startScanning() {
        val adapter = bluetoothAdapter ?: return
        try {
            if (!isReceiverRegistered) {
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                context.registerReceiver(discoveryReceiver, filter)
                isReceiverRegistered = true
            }
            if (adapter.isDiscovering) {
                adapter.cancelDiscovery()
            }
            adapter.startDiscovery()
        } catch (e: Exception) {
            Log.w(TAG, "Start discovery error: ${e.message}")
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter?.cancelDiscovery()
            }
            if (isReceiverRegistered) {
                context.unregisterReceiver(discoveryReceiver)
                isReceiverRegistered = false
            }
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    fun connectToDevice(device: BluetoothDevice) {
        stopClientConnection()
        stopScanning()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val socket = device.createRfcommSocketToServiceRecord(LUDO_BT_UUID)
                socket.connect()
                clientSocket = socket

                val writer = PrintWriter(socket.outputStream, true)
                val reader = BufferedReader(InputStreamReader(socket.inputStream))
                clientWriter = writer

                withContext(Dispatchers.Main) {
                    onPeerConnected("host")
                }

                clientReadJob = launch {
                    try {
                        while (isActive && socket.isConnected) {
                            val line = reader.readLine() ?: break
                            val packet = P2PProtocol.decodePacket(line)
                            if (packet != null) {
                                withContext(Dispatchers.Main) {
                                    onPacketReceived(packet, "host")
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.d(TAG, "BT Client read error: ${e.message}")
                    } finally {
                        withContext(Dispatchers.Main) {
                            onPeerDisconnected("host")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onError("Bluetooth connection failed: ${e.localizedMessage ?: e.message}")
                }
            }
        }
    }

    // ----------------------------------------------------
    // SENDING
    // ----------------------------------------------------

    fun sendPacketToAll(packet: P2PPacket) {
        val payload = P2PProtocol.encodePacket(packet)
        coroutineScope.launch(Dispatchers.IO) {
            for (client in connectedClients.values) {
                try {
                    client.writer.println(payload)
                } catch (_: Exception) {}
            }
            clientWriter?.let { writer ->
                try {
                    writer.println(payload)
                } catch (_: Exception) {}
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
        stopScanning()

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
