package com.elementz.heartrate2osc

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val prefs = getSharedPreferences("osc_prefs", MODE_PRIVATE)

        val ipField = findViewById<EditText>(R.id.ipField)
        val portField = findViewById<EditText>(R.id.portField)
        val saveBtn = findViewById<Button>(R.id.saveBtn)

        ipField.setText(prefs.getString("ip", "10.0.2.2"))
        portField.setText(prefs.getInt("port", 9000).toString())

        saveBtn.setOnClickListener {
            prefs.edit()
                .putString("ip", ipField.text.toString())
                .putInt("port", portField.text.toString().toInt())
                .apply()

            val serviceIntent = Intent(this, SensorService::class.java)
            stopService(serviceIntent)
            startForegroundService(serviceIntent)

            finish()
        }
    }
}
