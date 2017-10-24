package io.github.plenglin.kiwicontrol

object Constants {

    val TAG = "kiwibot"

    val BITGREES_PER_DEGREE = 4096

    fun bitgreesToDegrees(a: Int): Double = a.toDouble() / BITGREES_PER_DEGREE

    fun degreesToBitgrees(a: Double): Int = (a * BITGREES_PER_DEGREE).toInt()

    val INFO_PING_PERIOD = 250L

    val CTRL_SEND_PERIOD = 100L

}