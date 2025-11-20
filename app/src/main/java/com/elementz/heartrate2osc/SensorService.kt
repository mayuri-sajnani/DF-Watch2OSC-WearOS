package com.elementz.heartrate2osc

import android.app.*
import android.content.Intent
import android.hardware.*
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import android.content.pm.ServiceInfo

class SensorService : Service(), SensorEventListener {

    private val tag = "SensorService"
    private lateinit var sensorManager: SensorManager
    private var sender: OscSender? = null
    private var lastHeartRate: Float? = null
    private var lastAccel = FloatArray(3)
    private var lastGyro = FloatArray(3)
    private var lastLinearAccel = FloatArray(3)
    private var lastRotation = FloatArray(5)
    private var lastOrientation = FloatArray(3)
    private var lastStepCount: Float? = null
    private var lastGravity = FloatArray(3)

    override fun onCreate() {
        super.onCreate()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        startForegroundServiceNotification()

        //change ip and port here
//        sender = OscSender("10.0.2.2", 9000)
        val prefs = getSharedPreferences("osc_prefs", MODE_PRIVATE)

        val ip = prefs.getString("ip", "10.0.2.2")!!
        val port = prefs.getInt("port", 9000)

        sender = OscSender(ip, port)

        registerSensors()
        Log.d(tag, "Sensor streaming started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        sender?.close()
        Log.d(tag, "Sensor streaming stopped")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundServiceNotification() {
        val channelId = "sensor_stream_channel"

        val channel = NotificationChannel(
            channelId,
            "Sensor Streaming",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("OSC Streaming Active")
            .setContentText("Sensors broadcasting")
            .setSmallIcon(R.drawable.service_icon)
            .setOngoing(true)
            .setSilent(true)
            .build()

        startForeground(
            1,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
        )
    }

    private fun registerSensors() {
        val hr = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        val rot = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        hr?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
        accel?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        gyro?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        rot?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {

            Sensor.TYPE_HEART_RATE -> {
                val hr = event.values[0]
                if (lastHeartRate == null || hr != lastHeartRate) {
                    lastHeartRate = hr
                    sender?.sendList("/heartrate", listOf(hr))
                }
            }

            Sensor.TYPE_ACCELEROMETER -> {
                if (!event.values.contentEquals(lastAccel)) {
                    lastAccel = event.values.clone()
                    sender?.sendList("/accelerometer", lastAccel.toList())
                }
            }

            Sensor.TYPE_GYROSCOPE -> {
                if (!event.values.contentEquals(lastGyro)) {
                    lastGyro = event.values.clone()
                    sender?.sendList("/gyroscope", lastGyro.toList())
                }
            }

            Sensor.TYPE_LINEAR_ACCELERATION -> {
                if (!event.values.contentEquals(lastLinearAccel)) {
                    lastLinearAccel = event.values.clone()
                    sender?.sendList("/linear_acceleration", lastLinearAccel.toList())
                }
            }

            Sensor.TYPE_ROTATION_VECTOR -> {
                if (!event.values.contentEquals(lastRotation)) {
                    lastRotation = event.values.clone()
                    sender?.sendList("/rotation_vector", lastRotation.toList())
                }
            }

            Sensor.TYPE_ORIENTATION -> {
                if (!event.values.contentEquals(lastOrientation)) {
                    lastOrientation = event.values.clone()
                    sender?.sendList("/orientation", lastOrientation.toList())
                }
            }

            Sensor.TYPE_STEP_COUNTER -> {
                val steps = event.values[0]
                if (lastStepCount == null || steps != lastStepCount) {
                    lastStepCount = steps
                    sender?.sendList("/step_counter", listOf(steps))
                }
            }

            Sensor.TYPE_GRAVITY -> {
                if (!event.values.contentEquals(lastGravity)) {
                    lastGravity = event.values.clone()
                    sender?.sendList("/gravity", lastGravity.toList())
                }
            }
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
