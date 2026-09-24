package com.swarsatya.myapplication.call

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.swarsatya.myapplication.model.CallSession

class CallStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED &&
            intent.action != "android.intent.action.PHONE_STATE"
        ) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return

        when (state) {
            TelephonyManager.EXTRA_STATE_RINGING -> {
                // INCOMING CALL RINGING
                CallSessionStore.reset()
                OverlayHelper.hide(context)

                val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                    ?: intent.getStringExtra("incoming_number")
                    ?: "Unknown Caller"

                Log.d(TAG, "RINGING | Incoming Call From: $incomingNumber")

                val session = CallSession(
                    callerNumber = incomingNumber,
                    startTimeMs = System.currentTimeMillis(),
                    isIncoming = true // Marked as INCOMING
                )
                CallSessionStore.current = session

                val isBlocked = PreCallChecker.isBlacklisted(incomingNumber)
                session.isBlacklistedPreCall = isBlocked

                if (isBlocked) {
                    OverlayHelper.showBlacklistWarning(context, incomingNumber)
                } else {
                    OverlayHelper.showInfo(context, "Incoming Call: $incomingNumber\nPre-Call Check: CLEAN")
                }
            }

            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                Log.d(TAG, "OFFHOOK | Call Connected or Dialing")

                var session = CallSessionStore.current

                // If RINGING didn't happen first, this is an OUTGOING call!
                if (session == null || !session.isIncoming) {
                    val number = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        ?: intent.getStringExtra("incoming_number")
                        ?: "Outgoing Call"

                    Log.d(TAG, "OFFHOOK | Outgoing Call To: $number")

                    session = CallSession(
                        callerNumber = number,
                        startTimeMs = System.currentTimeMillis(),
                        isIncoming = false // Marked as OUTGOING
                    )
                    CallSessionStore.current = session

                    // Show "Connecting" banner while dialing (No risk % yet!)
                    OverlayHelper.showInfo(context, "Outgoing Call: $number\nConnecting call...")
                } else {
                    // Incoming call was answered
                    OverlayHelper.hide(context)
                }

                // Start Detection Service
                val serviceIntent = Intent(context, VoiceDetectionService::class.java)
                context.startForegroundService(serviceIntent)
            }

            TelephonyManager.EXTRA_STATE_IDLE -> {
                // CALL ENDED / HUNG UP
                Log.d(TAG, "IDLE | Call Ended")

                context.stopService(Intent(context, VoiceDetectionService::class.java))

                CallSessionStore.current?.let { session ->
                    session.endTimeMs = System.currentTimeMillis()
                    session.durationSec = ((session.endTimeMs ?: session.startTimeMs) - session.startTimeMs) / 1000
                    Log.d(TAG, "LOCAL CDR => Caller: ${session.callerNumber}, Duration: ${session.durationSec}s, Risk: ${session.riskScore}")
                }

                OverlayHelper.hide(context)
                CallSessionStore.reset()
            }
        }
    }

    companion object {
        private const val TAG = "SwarSatyaCall"
    }
}