package io.github.plenglin.kiwicontrol

import android.app.Activity
import android.app.AlertDialog
import android.app.DialogFragment
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView

class RobotControllerActivity : Activity() {

    private lateinit var bluetooth: BluetoothAdapter
    private lateinit var connected: BluetoothSocket

    private lateinit var knob: KnobView
    private lateinit var joystick: JoystickView
    private lateinit var connectBtn: Button

    private lateinit var connectedIndicator: TextView

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

            Log.d(Constants.TAG, "clicked on connectBtn")

            val paired = bluetooth.bondedDevices
            val arrayAdapter = ArrayAdapter<BluetoothListElement>(this, android.R.layout.select_dialog_item)
            for (d in paired) {
                arrayAdapter.add(BluetoothListElement.fromBluetoothDevice(d))
            }

            val builder = AlertDialog.Builder(this)

            builder.setNegativeButton("Cancel", { dialog, which -> dialog.dismiss() })

            builder.setAdapter(arrayAdapter, { dialog, which ->
                Log.d(Constants.TAG, "picked $which")
                val dev = arrayAdapter.getItem(which).device
                connected = dev.createRfcommSocketToServiceRecord(dev.uuids[0].uuid)
                connectedIndicator.text = dev.name.subSequence(0, minOf(dev.name.length, 6))
                connectBtn.backgroundTintList = resources.getColorStateList(R.color.connected, theme)
            })

            builder.show()

        }
        connectBtn.backgroundTintList = resources.getColorStateList(R.color.disconnected, theme)

        connectedIndicator = findViewById(R.id.connectionState) as TextView
        connectedIndicator.text = "----"
    }

    fun setVisibilityState() {
        this.window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
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