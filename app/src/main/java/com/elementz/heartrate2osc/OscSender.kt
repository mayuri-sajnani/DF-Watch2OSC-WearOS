package com.elementz.heartrate2osc

import android.util.Log
import com.illposed.osc.OSCMessage
import com.illposed.osc.transport.udp.OSCPortOut
import java.net.InetAddress
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.atomic.AtomicBoolean

class OscSender(ip: String, port: Int) {

    private val queue = LinkedBlockingQueue<Pair<String, List<Float>>>()
    private val running = AtomicBoolean(true)
    private var osc: OSCPortOut? = null

    init {
        try {
            val address = InetAddress.getByName(ip)
            osc = OSCPortOut(address, port)
        } catch (e: Exception) {
            Log.e("OSC", "Failed to create OSCPortOut: ${e.message}")
        }

        // Background worker thread ONLY for OSC sending
        Thread {
            while (running.get()) {
                try {
                    val (addr, values) = queue.take()
                    val msg = OSCMessage(addr, values)
                    osc?.send(msg)
                } catch (e: Exception) {
                    Log.e("OSC", "Send failed -> ${e.message}")
                }
            }
        }.apply {
            name = "OSC-Sender-Thread"
            priority = Thread.MIN_PRIORITY
            start()
        }
    }

    fun sendList(address: String, values: List<Float>) {
        queue.offer(address to values)
    }

    fun close() {
        running.set(false)
        try {
            osc?.close()
        } catch (_: Exception) {}
    }
}
