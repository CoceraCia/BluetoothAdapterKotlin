package com.coceracia.bluetoothadapter.model

import android.bluetooth.BluetoothDevice

data class BluDevice(val device: BluetoothDevice, var isConnected: Boolean = false)