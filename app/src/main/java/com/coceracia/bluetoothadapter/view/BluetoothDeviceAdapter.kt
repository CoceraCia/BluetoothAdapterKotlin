package com.coceracia.bluetoothadapter.view

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresPermission
import androidx.recyclerview.widget.RecyclerView
import com.coceracia.bluetoothadapter.R
import com.coceracia.bluetoothadapter.model.BluDevice

class BluDeviceAdapter(private val list: MutableList<BluDevice>,
    private val onClick: (BluDevice) -> Unit) : RecyclerView.Adapter<BluDeviceAdapter.BluDeviceViewHolder>() {
    class BluDeviceViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val name = itemView.findViewById<TextView>(R.id.tvTitleDevice)
        val connected = itemView.findViewById<TextView>(R.id.tvDeviceBondState)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BluDeviceAdapter.BluDeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_device, parent, false)
        return BluDeviceViewHolder(view)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    override fun onBindViewHolder(
        holder: BluDeviceViewHolder,
        position: Int
    ) {
        val bludev = list[position]
        val device = bludev.device
        holder.name.text =  if (device.name == null) device.address else device.name
        if (bludev.isConnected){
            holder.connected.text = "connected"
        } else {
            var state = when (device.bondState) {
                BluetoothDevice.BOND_BONDED -> "paired"
                else -> {"unknown"}
            }
            holder.connected.text = state
        }

        holder.itemView.setOnClickListener {
            onClick(bludev)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun addDevice(device: BluDevice) {
        list.add(device)
        notifyDataSetChanged()
    }
}