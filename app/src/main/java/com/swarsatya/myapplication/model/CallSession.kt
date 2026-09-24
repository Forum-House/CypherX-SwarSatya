package com.swarsatya.myapplication.model

data class CallSession(
    val callerNumber: String,
    val startTimeMs: Long,
    var endTimeMs: Long? = null,
    var durationSec: Long? = null,
    var riskScore: Float = 0f,
    var verdict: String = "UNKNOWN",
    var isBlacklistedPreCall: Boolean = false,
    var isIncoming: Boolean = false // Track if call is incoming or outgoing
)