package com.elementz.heartrate2osc

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.view.MotionEvent
import android.widget.ImageView
import android.view.WindowManager
import android.os.PowerManager

class MainActivity : AppCompatActivity() {

    private var tapCount = 0
    private var lastTapTime = 0L
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val now = System.currentTimeMillis()

            if (now - lastTapTime < 500) {
                tapCount++
            } else {
                tapCount = 1
            }

            lastTapTime = now

            if (tapCount == 3) {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
        }
        return super.onTouchEvent(event)
    }

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.FOREGROUND_SERVICE_HEALTH,
        Manifest.permission.HIGH_SAMPLING_RATE_SENSORS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val bg = findViewById<ImageView>(R.id.bgImage)
        val anim = android.view.animation.AnimationUtils.loadAnimation(this, R.anim.rotate_slow)
        bg.startAnimation(anim)

        ensureAllPermissions {
            startSensorService()
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )

        //prevent sleep
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "HR2OSC:WakeLock"
        )
        wakeLock?.acquire()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
    }

    private fun ensureAllPermissions(onGranted: () -> Unit) {
        val missing = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isEmpty()) {
            onGranted()
        } else {
            ActivityCompat.requestPermissions(
                this,
                missing.toTypedArray(),
                100
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults.all { it == PackageManager.PERMISSION_GRANTED }
        ) {
            startSensorService()
        }
    }

    private fun startSensorService() {
        val intent = Intent(this, SensorService::class.java)
        startForegroundService(intent)
    }
}
