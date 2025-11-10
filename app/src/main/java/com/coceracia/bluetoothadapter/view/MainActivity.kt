package com.coceracia.bluetoothadapter.view

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coceracia.bluetoothadapter.R
import com.coceracia.bluetoothadapter.model.BluDevice
import com.coceracia.bluetoothadapter.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val devList = mutableListOf<BluDevice>()
    private val devListPaired = mutableListOf<BluDevice>()


    private val requestsPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        permissions.forEach { (permission, isGranted) ->
            if (!isGranted) {
                AlertDialog.Builder(this).setTitle("ERROR")
                    .setMessage("Permissions needs to be enabled in order to use this app")
                    .setNegativeButton("OK") { _, _ ->
                        finish()
                    }.show()
            }
        }
        enBluetooth()
    }

    private val requestBluetooth = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_CANCELED) {
            finish()
        } else {
            enLocation()
        }
    }

    private val requestLocation = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_CANCELED) {
            finish()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN])
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val recyclerViewOtherDevices = findViewById<RecyclerView>(R.id.rvDevicesShown)
        val adapterOd = BluDeviceAdapter(devList, onClick = { blu ->
            connectDevice(blu.device)
        })
        recyclerViewOtherDevices.layoutManager = LinearLayoutManager(this)
        recyclerViewOtherDevices.adapter = adapterOd


        val recyclerViewPaired = findViewById<RecyclerView>(R.id.rvPairedDevicesShown)
        val adapterPaired = BluDeviceAdapter(devListPaired, onClick = { blu ->
            connectDevice(blu.device)
        })
        recyclerViewPaired.layoutManager = LinearLayoutManager(this)
        recyclerViewPaired.adapter = adapterPaired



        lifecycleScope.launch {
            mainViewModel.device.collect { device ->
                if (!mainViewModel.deviceRepeated(device, devList)) {
                    adapterOd.addDevice(BluDevice(device, false))
                    recyclerViewOtherDevices.adapter?.notifyDataSetChanged()
                }
            }
        }

        lifecycleScope.launch {
            mainViewModel.result.collect { r ->
                when (r) {
                    "BT FINISHED" -> searchDevices()
                }
            }
        }

        lifecycleScope.launch {
            mainViewModel.newDeviceConn.collect { d ->
                Log.d("con", "connected")
                onNewConnectedDevice(d)
                recyclerViewOtherDevices.adapter?.notifyDataSetChanged()
                recyclerViewPaired.adapter?.notifyDataSetChanged()
            }
        }


        enablePermission()
        searchDevices()

        mainViewModel.obtainPairedDevices(devListPaired)
        recyclerViewPaired.adapter?.notifyDataSetChanged()
    }


    override fun onDestroy() {
        super.onDestroy()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun onNewConnectedDevice(device: BluetoothDevice) {
        val bludev = mainViewModel.findBluDevice(device, devList)
        if (bludev != null){
            devList.remove(bludev)
            devListPaired.add(bludev!!)
        }
        mainViewModel.connectedDevice(device, devListPaired)
        AlertDialog.Builder(this).setTitle("CONNECTED")
            .setMessage("${device.name} succesfully connected").setPositiveButton("OK", null).show()
    }

    @androidx.annotation.RequiresPermission(allOf = [android.Manifest.permission.BLUETOOTH_SCAN, android.Manifest.permission.BLUETOOTH_CONNECT])
    fun connectDevice(device: BluetoothDevice){
        val name = if (device.name != null) device.name else device.address
        AlertDialog.Builder(this).setTitle("BLUETOOTH CONNECT")
            .setMessage("Would you like to establish a connection with $name")
            .setPositiveButton("OK") { _, _ ->
                mainViewModel.connectDevice(device)
                Toast.makeText(this, "Establishing connection...", Toast.LENGTH_LONG).show()
            }.setNegativeButton("NO") { _, _ -> }.show()
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_SCAN)
    fun searchDevices() {
        mainViewModel.obtainDevices(this)
        Toast.makeText(this, "SEARCHING", Toast.LENGTH_LONG).show()
    }

    private fun enablePermission(): Boolean {
        var perm: kotlin.Array<String>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12 or upper
            perm = arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
            )
        } else {
            // Android 10 or above
            perm = arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        if (!perm.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            requestsPermissions.launch(perm)
            return false
        } else {
            enBluetooth()
            return true
        }
    }


    private fun enBluetooth() {
        if (!mainViewModel.bluetoothStatus(this)) {
            val intentbl = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            requestBluetooth.launch(intentbl)
        } else {
            enLocation()
        }
    }

    private fun enLocation() {
        if (!mainViewModel.locationStatus(this)) {
            AlertDialog.Builder(this).setTitle("APP REQUIRIMENTS")
                .setMessage("Location needs to be enabled in order to use this app")
                .setPositiveButton("OK") { _, _ ->
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                }.setNegativeButton("EXIT") { _, _ -> finish() }.setCancelable(false).show()
        }
    }
}