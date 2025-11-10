package com.coceracia.bluetoothadapter.viewmodel

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coceracia.bluetoothadapter.model.BluDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

class MainViewModel() : ViewModel() {
    private val _error: MutableSharedFlow<String> = MutableSharedFlow<String>()
    val error: MutableSharedFlow<String> get() = _error

    private val _result: MutableSharedFlow<String> = MutableSharedFlow<String>()
    val result: MutableSharedFlow<String> get() = _result

    private val _device: MutableSharedFlow<BluetoothDevice> = MutableSharedFlow<BluetoothDevice>()
    val device: MutableSharedFlow<BluetoothDevice> get() = _device

    private val _newDeviceConn: MutableSharedFlow<BluetoothDevice> =
        MutableSharedFlow<BluetoothDevice>()
    val newDeviceConn: MutableSharedFlow<BluetoothDevice> get() = _newDeviceConn

    lateinit var blManager: BluetoothManager
    lateinit var blAdapter: BluetoothAdapter

    fun locationStatus(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun bluetoothStatus(context: Context): Boolean {
        try {
            blManager = context.getSystemService(BluetoothManager::class.java)
            blAdapter = blManager.adapter

            if (blAdapter == null || !blAdapter.isEnabled) {
                return false
            } else {
                return true
            }
        } catch (e: Exception) {
            Log.e("BluConnError", "$e")
            throw e
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun obtainPairedDevices(list: MutableList<BluDevice>) {
        val listBounded = blAdapter.bondedDevices
        listBounded.forEach { device ->
            if (device != null && list.all { it.device.address != device.address }) {
                list.add(BluDevice(device))
            }
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun obtainDevices(context: Context) {
        val receiver = object : BroadcastReceiver() {
            @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
            override fun onReceive(context: Context?, intent: Intent?) {
                val device = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent?.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE,
                        BluetoothDevice::class.java
                    )
                } else {
                    intent?.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                }
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        Log.d(
                            "BT",
                            "📡 Encontrado: ${device?.name} (${device?.address} | ${device?.bondState})"
                        )
                        if (device != null) {
                            viewModelScope.launch {
                                _device.emit(device)
                            }
                        }
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        if (device?.bondState == BluetoothDevice.BOND_BONDED) {
                            viewModelScope.launch {
                                _newDeviceConn.emit(device)
                            }
                        }
                        Log.d("BT", "✅ Emparejado correctamente con ${device?.name}")
                    }

                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                        Log.d("BT", "✅ Búsqueda finalizada")
                        context?.unregisterReceiver(this)
                        viewModelScope.launch {
                            _result.emit("BT FINISHED")
                        }
                    }
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        }

        context.registerReceiver(receiver, filter)

        Log.d("discovery", blAdapter?.startDiscovery().toString())
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT])
    fun connectDevice(device: BluetoothDevice) {
        viewModelScope.launch {
            try {

                if (blAdapter?.isDiscovering!!) {
                    blAdapter.cancelDiscovery()
                }
                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    Log.d("BT", "AlreadyBonded")
                    device.javaClass.getMethod("removeBond").invoke(device)
                    delay(500)
                }
                device.createBond()
            } catch (e: IOException) {
                Log.d("BT", "error al conectar: $e")
            }
        }
    }

    fun deviceRepeated(device: BluetoothDevice, list: MutableList<BluDevice>): Boolean {
        list.forEach {
            if (device.address == it.device.address) {
                return true
            }
        }
        return false
    }

    fun findBluDevice(device: BluetoothDevice, list: MutableList<BluDevice>): BluDevice? {
        list.forEach {
            if (device.address == it.device.address) {
                return it
            }
        }
        return null
    }

    fun connectedDevice(device: BluetoothDevice, list:  MutableList<BluDevice>){
        list.forEach {
            if (device.address == it.device.address) {
                it.isConnected = true
            }
        }
    }
}