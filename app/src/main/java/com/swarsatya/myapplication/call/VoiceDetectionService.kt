package com.swarsatya.myapplication.call

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.swarsatya.myapplication.R

class VoiceDetectionService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private var tick = 0
    private var isSpeechStarted = false // VAD Gate State

    private val analyzeRunnable = object : Runnable {
        override fun run() {
            val session = CallSessionStore.current
            if (session == null) {
                Log.w(TAG, "No active call session found! Stopping analysis.")
                return
            }

            tick++

            // =========================================================================
            // VAD GATE LOGIC (Voice Activity Detection)
            // =========================================================================
            // For Outgoing Calls: Do NOT evaluate or show risk until speech is detected!
            // In a real call, VAD detects when the recipient says "Hello?".
            // For testing/demo, speech activates on tick 6 (simulating recipient answering).
            if (!session.isIncoming && !isSpeechStarted) {
                if (tick < 6) {
                    Log.d(TAG, "Outgoing Call | Ringing / Waiting for speech... (Second $tick)")
                    OverlayHelper.showInfo(
                        this@VoiceDetectionService,
                        "Outgoing Call: ${session.callerNumber}\nDialing / Waiting for recipient to answer..."
                    )
                    handler.postDelayed(this, 1000L)
                    return
                } else {
                    // Recipient answered & spoke! VAD Gate Opens!
                    isSpeechStarted = true
                    Log.d(TAG, "VAD GATE OPEN: Human speech detected! Starting AI Risk Engine.")
                }
            } else {
                // Incoming calls start VAD analysis immediately
                isSpeechStarted = true
            }

            // =========================================================================
            // AI RISK ENGINE (Runs ONLY when speech is active)
            // =========================================================================
            val mockScore = mockRiskScore()
            session.riskScore = mockScore
            session.verdict = if (mockScore >= 0.80f) "FAKE" else "REAL"

            Log.d(TAG, "Second $tick | Active Speech Detected | Caller: ${session.callerNumber} | Risk: ${"%.2f".format(mockScore)}")

            if (mockScore >= 0.80f) {
                OverlayHelper.showHighRisk(
                    this@VoiceDetectionService,
                    session.callerNumber,
                    mockScore
                )

                FraudReporter.reportToBackend(
                    callerNumber = session.callerNumber,
                    riskScore = mockScore,
                    audioHash = "0xDEMO_HASH_${System.currentTimeMillis()}"
                )
            } else {
                OverlayHelper.showSafe(
                    this@VoiceDetectionService,
                    session.callerNumber,
                    mockScore
                )
            }

            handler.postDelayed(this, 1000L)
        }
    }

    override fun onCreate() {
        super.onCreate()
        startAsForeground()
        tick = 0
        isSpeechStarted = false
        handler.postDelayed(analyzeRunnable, 1000L)
        Log.d(TAG, "Detection service started")
    }

    override fun onDestroy() {
        handler.removeCallbacks(analyzeRunnable)
        tick = 0
        isSpeechStarted = false
        Log.d(TAG, "Detection service stopped + cleanup")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startAsForeground() {
        val channelId = "swarsatya_detect"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SwarSatya Call Protection",
                NotificationManager.IMPORTANCE_LOW
            )
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("SwarSatya Active")
            .setContentText("Monitoring live call for AI voice clone risk")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

        startForeground(101, notification)
    }

    private fun mockRiskScore(): Float {
        // High risk alert triggers on 10th second of active conversation
        return if (tick == 10 || tick == 18) 0.92f
        else (10..35).random() / 100f
    }

    companion object {
        private const val TAG = "SwarSatyaDetect"
    }
}