package io.github.plenglin.kiwicontrol

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.util.Log
import java.io.*
import java.util.*

class RobotInterface(val btDevice: BluetoothDevice) {

    var robotState = RobotState(0.0, 0.0)
    lateinit var socket: BluetoothSocket
    lateinit var inputThread: Thread
    lateinit var outputStream: PrintStream

    val requestPinger = Handler()

    fun initializeDevice() {
        btDevice.createRfcommSocketToServiceRecord(btDevice.uuids[0].uuid)

        outputStream = PrintStream(socket.outputStream)
    }

    fun initializeThreads() {
        inputThread = Thread(Runnable {
            val scanner = Scanner(socket.inputStream)
            Log.i(Constants.TAG, "initializing input thread")
            scanner.useDelimiter("""\nOK\n""")
            while (true) {
                val inputString = scanner.next()
                Log.d(Constants.TAG, inputString)
                val inputData = inputString.split(" ").toMutableList()
                val key = inputData.removeAt(0)
                val serialized = paramsToMap(inputData)
                when (key) {
                    "BER" -> {
                        robotState.bearing = Constants.bitgreesToDegrees(serialized["head"]?.toInt() ?: 0)
                    }
                    "TRG" -> {
                        robotState.targetBearing = Constants.bitgreesToDegrees(serialized["value"]?.toInt() ?: 0)
                    }
                }
            }
        })
        inputThread.start()
        requestPinger.post { requestRobotState() }
    }

    fun requestRobotState() {
        Log.d(Constants.TAG, "pinging for robot state")

        synchronized (outputStream) {
            outputStream.println("b")
            outputStream.println("g")
        }

        requestPinger.postDelayed({ requestRobotState() }, Constants.INFO_PING_PERIOD)
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