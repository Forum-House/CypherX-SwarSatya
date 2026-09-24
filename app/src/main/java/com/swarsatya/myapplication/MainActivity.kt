package com.swarsatya.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.swarsatya.myapplication.call.OverlayHelper
import com.swarsatya.myapplication.call.PreCallChecker

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        val btnPermissions = findViewById<Button>(R.id.btnPermissions)
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnTestBlacklist = findViewById<Button>(R.id.btnTestBlacklist)
        val btnTestHighRisk = findViewById<Button>(R.id.btnTestHighRisk)

        btnPermissions.setOnClickListener { askRuntimePermissions() }
        btnOverlay.setOnClickListener { OverlayHelper.askOverlayPermission(this) }

        btnTestBlacklist.setOnClickListener {
            val number = "9999999999"
            val blocked = PreCallChecker.isBlacklisted(number)
            statusText.text = "Pre-call check $number => blacklisted=$blocked"
            if (blocked) OverlayHelper.showBlacklistWarning(this, number)
        }

        btnTestHighRisk.setOnClickListener {
            OverlayHelper.showHighRisk(this, "+91 98XXXX1122", 0.94f)
            statusText.text = "Simulated high-risk overlay shown"
        }

        statusText.text = "Ready. Grant permissions, then call this phone from another number."
    }

    private fun askRuntimePermissions() {
        val needed = mutableListOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            needed.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        needed.add(Manifest.permission.READ_CALL_LOG)

        val missing = needed.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        } else {
            Toast.makeText(this, "All runtime permissions already granted", Toast.LENGTH_SHORT).show()
        }
    }
}