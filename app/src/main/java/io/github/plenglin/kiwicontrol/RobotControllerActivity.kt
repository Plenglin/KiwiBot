package io.github.plenglin.kiwicontrol

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.text.DecimalFormat

class RobotControllerActivity : Activity() {

    private lateinit var bluetooth: BluetoothAdapter
    private lateinit var robot: RobotInterface

    private lateinit var knob: KnobView
    private lateinit var joystick: JoystickView
    private lateinit var connectBtn: Button

    private lateinit var connectedIndicator: TextView
    private lateinit var bearingIndicator: TextView
    private lateinit var bearingTargetIndicator: TextView

    private lateinit var ctrlSendHandler: Handler
    private lateinit var tableUpdateHandler: Handler

    private val threeDecFmt = "%.3f"

    private val bluetoothConnectionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d(Constants.TAG, "received bluetooth intent $intent")
            val action = intent!!.action
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            Log.d(Constants.TAG, "device is $device")
            if (device != robot.btDevice) return
            when (action) {
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    Log.i(Constants.TAG, "bluetooth connected")
                    onBluetoothConnected()
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    Log.i(Constants.TAG, "bluetooth disconnected")
                    onBluetoothDisconnected()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setVisibilityState()
        setContentView(R.layout.activity_robot_controller)

        bluetooth = BluetoothAdapter.getDefaultAdapter()

        knob = findViewById(R.id.bearingKnob) as KnobView

        joystick = findViewById(R.id.translationJoystick) as JoystickView
        joystick.isClickable = true

        connectBtn = findViewById(R.id.connectBtn) as Button
        connectBtn.setOnClickListener {

            Log.i(Constants.TAG, "clicked on connectBtn")

            val paired = bluetooth.bondedDevices
            val arrayAdapter = ArrayAdapter<BluetoothListElement>(this, android.R.layout.select_dialog_item)
            for (d in paired) {
                arrayAdapter.add(BluetoothListElement.fromBluetoothDevice(d))
            }

            val builder = AlertDialog.Builder(this)

            builder.setNegativeButton("Cancel", { dialog, which -> dialog.dismiss() })

            // When a device is picked
            builder.setAdapter(arrayAdapter, { dialog, which ->
                Log.i(Constants.TAG, "picked $which")
                val dev = arrayAdapter.getItem(which).device
                robot = RobotInterface(this, dev)
                robot.initializeDevice()

                registerBluetoothReceiver()
            })

            builder.show()

        }
        connectBtn.backgroundTintList = resources.getColorStateList(R.color.disconnected, theme)

        connectedIndicator = findViewById(R.id.connectionState) as TextView
        bearingIndicator = findViewById(R.id.dataBearing) as TextView
        bearingTargetIndicator = findViewById(R.id.dataTargetBearing) as TextView

        onBluetoothDisconnected()
    }

    fun updateTableHandler() {
        bearingIndicator.text = threeDecFmt.format(robot.robotState.bearing)
        bearingTargetIndicator.text = threeDecFmt.format(robot.robotState.targetBearing)
        tableUpdateHandler.postDelayed({ updateTableHandler() }, Constants.INFO_PING_PERIOD)
    }

    fun sendControlDataHandler() {
        val r = (joystick.touchRadius * 128).toInt()
        val t = joystick.theta
        val angle = Constants.degreesToBitgrees(Math.toDegrees(knob.theta))

        val roboTarget = robot.robotState.targetBearing
        val roboGyro = robot.robotState.bearing
        Log.d(Constants.TAG, "gyrotarget=$angle\trobotarget=$roboTarget\trobogyro=$roboGyro")
        synchronized (robot.outputStream) {
            robot.outputStream.println("ta$r")
            robot.outputStream.println("g$angle")
        }
        ctrlSendHandler.postDelayed({ sendControlDataHandler() }, Constants.CTRL_SEND_PERIOD)
    }

    fun registerBluetoothReceiver() {
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }

        this.registerReceiver(bluetoothConnectionReceiver, filter)
    }

    fun setVisibilityState() {
        this.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
    }

    fun onBluetoothConnected() {
        robot.initializeThreads()
        synchronized (robot.outputStream) {
            robot.outputStream.println("d3")
        }
        connectedIndicator.text = robot.btDevice.name.subSequence(0, minOf(robot.btDevice.name.length, 6))
        connectBtn.backgroundTintList = resources.getColorStateList(R.color.connected, theme)

        ctrlSendHandler = Handler()
        ctrlSendHandler.post { sendControlDataHandler() }
        tableUpdateHandler = Handler()
        tableUpdateHandler.post { updateTableHandler() }
    }

    fun onBluetoothDisconnected() {
        connectedIndicator.text = "------"
        connectBtn.backgroundTintList = resources.getColorStateList(R.color.disconnected, theme)
    }

    fun onBluetoothConnectionFailure() {
        Log.e(Constants.TAG, "failure to connect!")
        Toast.makeText(applicationContext, "Failed to connect", Toast.LENGTH_SHORT).show();
    }

}

data class BluetoothListElement(val device: BluetoothDevice) {

    override fun toString(): String {
        return device.name
    }

    companion object {
        fun fromBluetoothDevice(device: BluetoothDevice): BluetoothListElement {
            return BluetoothListElement(device)
        }
    }
}