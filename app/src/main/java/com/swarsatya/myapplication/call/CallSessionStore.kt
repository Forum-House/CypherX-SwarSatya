package com.swarsatya.myapplication.call

import com.swarsatya.myapplication.model.CallSession

object CallSessionStore {
    @Volatile
    var current: CallSession? = null

    // Clears old session when call ends
    fun reset() {
        current = null
    }
}