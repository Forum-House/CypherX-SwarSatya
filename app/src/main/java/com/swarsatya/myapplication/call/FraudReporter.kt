package com.swarsatya.myapplication.call

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.Executors

object FraudReporter {

    // Change this to your FastAPI server
    private const val REPORT_URL = "https://YOUR_BACKEND/report-fraud"
    private val executor = Executors.newSingleThreadExecutor()
    private val client = OkHttpClient()

    fun reportToBackend(callerNumber: String, riskScore: Float, audioHash: String) {
        executor.execute {
            try {
                val json = JSONObject()
                    .put("caller_number", callerNumber)
                    .put("risk_score", riskScore)
                    .put("audio_hash", audioHash)
                    .put("source", "android_app")
                    .toString()

                val body = json.toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url(REPORT_URL)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { resp ->
                    Log.d("SwarSatyaReport", "Backend response: ${resp.code}")
                }
            } catch (e: Exception) {
                // For offline phone testing, just log
                Log.e("SwarSatyaReport", "Backend not reachable (ok for local test): ${e.message}")
            }
        }
    }
}