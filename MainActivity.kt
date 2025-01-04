package com.example.bluetoothkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bluetoothStatusTv: TextView
    private lateinit var bluetoothIv: ImageView
    private lateinit var turnOnBtn: Button
    private lateinit var turnOffBtn: Button
    private lateinit var scanBtn: Button
    private lateinit var pairedTv: TextView
    private lateinit var emptyListTv: TextView
    private lateinit var devicesLv: ListView
    private lateinit var pairedDevicesAdapter: ArrayAdapter<String>
    private var bluetoothDevices = mutableListOf<String>()

    private val REQUEST_ENABLE_BT = 1
    private val REQUEST_PERMISSIONS = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UI elements
        bluetoothStatusTv = findViewById(R.id.bluetoothStatusTv)
        bluetoothIv = findViewById(R.id.bluetoothIv)
        turnOnBtn = findViewById(R.id.turnOnBtn)
        turnOffBtn = findViewById(R.id.turnOffBtn)
        scanBtn = findViewById(R.id.scanBtn)
        pairedTv = findViewById(R.id.pairedTv)
        emptyListTv = findViewById(R.id.emptyListTv)
        devicesLv = findViewById(R.id.devicesLv)

        // Initialize Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            bluetoothStatusTv.text = "Bluetooth not supported"
            return
        }

        // Check Bluetooth status
        if (bluetoothAdapter.isEnabled) {
            bluetoothStatusTv.text = "Bluetooth Status: Enabled"
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
        } else {
            bluetoothStatusTv.text = "Bluetooth Status: Disabled"
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
        }

        // Turn on Bluetooth
        turnOnBtn.setOnClickListener {
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (checkBluetoothPermissions()) {
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                } else {
                    requestBluetoothPermissions()
                }
            }
        }

        // Turn off Bluetooth
        turnOffBtn.setOnClickListener {
            if (checkBluetoothPermissions()) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                bluetoothAdapter.disable()
                bluetoothStatusTv.text = "Bluetooth Status: Disabled"
                bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
            } else {
                requestBluetoothPermissions()
            }
        }

        // Scan for devices
        scanBtn.setOnClickListener {
            if (checkBluetoothPermissions()) {
                bluetoothDevices.clear()
                val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
                registerReceiver(receiver, filter)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return@setOnClickListener
                }
                bluetoothAdapter.startDiscovery()
            } else {
                requestBluetoothPermissions()
            }
        }

        // Handle list item click to connect to device
        devicesLv.setOnItemClickListener { _, _, position, _ ->
            val deviceName = bluetoothDevices[position]
            connectToDevice(deviceName)
        }

        // Request Bluetooth permissions if necessary
        checkPermissions()
    }

    private fun checkPermissions() {
        // Check if permissions are granted for Bluetooth scanning and connection (for API >= 23)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ),
                    REQUEST_PERMISSIONS
                )
            }
        }
    }

    private fun checkBluetoothPermissions(): Boolean {
        // Check if the necessary permissions for Bluetooth are granted
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        } else {
            true // For devices below API 23, no need for runtime permission check
        }
    }

    private fun requestBluetoothPermissions() {
        // Request permissions if not already granted
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_PERMISSIONS
        )
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (BluetoothDevice.ACTION_FOUND == action) {
                val device: BluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)!!
                val deviceName = device.name ?: "Unknown Device"
                bluetoothDevices.add(deviceName)
                val adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_list_item_1, bluetoothDevices)
                devicesLv.adapter = adapter
                emptyListTv.visibility = if (bluetoothDevices.isEmpty()) {
                    Toast.makeText(this@MainActivity, "No devices found", Toast.LENGTH_SHORT).show()
                    TextView.VISIBLE
                } else {
                    TextView.GONE
                }
            }
        }
    }

    private fun connectToDevice(deviceName: String) {
        // Add your connection logic here
        Toast.makeText(this, "Connecting to $deviceName...", Toast.LENGTH_SHORT).show()

        // For now, we are just showing a toast when a device is selected
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
