package io.github.plenglin.kiwicontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import pl.info.czerwinski.physics2d.Vector
import java.io.*
import java.util.*

class RobotInterface(val activity: RobotControllerActivity, val btDevice: BluetoothDevice) {

    var robotState = RobotState(0.0, 0.0)
    lateinit var socket: BluetoothSocket
    lateinit var inputThread: Thread
    lateinit var outputStream: PrintStream

    val requestPinger = Handler()

    fun initializeDevice() {

        Log.d(Constants.TAG, "attempting to connect to $btDevice")
        socket = btDevice.createRfcommSocketToServiceRecord(btDevice.uuids[0].uuid)
        try {
            socket.connect()
        } catch (e: IOException) {
            activity.onBluetoothConnectionFailure()
        }

        activity.onBluetoothConnected()
    }

    fun initializeThreads() {
        outputStream = PrintStream(socket.outputStream)
        Log.i(Constants.TAG, "initializing input thread")
        inputThread = Thread(Runnable {
            Log.i(Constants.TAG, "starting input thread")
            val scanner = Scanner(socket.inputStream)
            scanner.useDelimiter("OK")
            while (true) {
                val inputString = scanner.next()
                val inputData = inputString.replace("\n", "").split(" ").toMutableList()
                val key = inputData.removeAt(0)
                val serialized = paramsToMap(inputData)
                Log.d(Constants.TAG, "received key: $key")
                when (key.trim()) {
                    "BER" -> {
                        Log.d(Constants.TAG, "$serialized")
                        robotState.bearing = Math.toRadians(Constants.bitgreesToDegrees(serialized["head"]?.toInt() ?: 0))
                    }
                    "TRG" -> {
                        Log.d(Constants.TAG, "$serialized")
                        robotState.targetBearing = Math.toRadians(Constants.bitgreesToDegrees(serialized["value"]?.toInt() ?: 0))
                    }
                }
            }
        })
        inputThread.start()
        requestPinger.post { requestRobotState() }
    }

    fun requestRobotState() {
        //Log.d(Constants.TAG, "pinging for robot state")

        synchronized (outputStream) {
            outputStream.println("b")
        }

        requestPinger.postDelayed({ requestRobotState() }, Constants.INFO_PING_PERIOD)
    }

    companion object {

        val vectorA = Vector.angle(Math.PI / 3)
        val vectorB = Vector.angle(Math.PI)
        val vectorC = Vector.angle(5 * Math.PI / 3)

    }

}

fun paramsToMap(data: List<String>): Map<String, String> {
    val matches = data.map { Regex("""(.*)=(.*)""").find(it) }
    val output = mutableMapOf<String, String>()
    for (s in matches) {
        if (s != null) {
            val key = s.groups[1]!!.value
            val value = s.groups[2]!!.value
            output[key] = value
        }
    }
    return output
}


data class RobotState(var bearing: Double, var targetBearing: Double)