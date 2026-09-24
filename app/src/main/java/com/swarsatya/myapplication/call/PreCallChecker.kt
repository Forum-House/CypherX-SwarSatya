
package com.swarsatya.myapplication.call

object PreCallChecker {

    // Temporary local mock blacklist for phone testing
    private val localBlacklist = setOf(
        "+911111111111",
        "1111111111",
        "9999999999"
    )

    fun isBlacklisted(phone: String): Boolean {
        val normalized = phone.replace(" ", "").replace("-", "")
        // Later replace this with Web3 read: contract.isBlacklisted(phone)
        return localBlacklist.any { normalized.endsWith(it.takeLast(10)) }
    }

    // Later: fetch registered voice hash
    fun getRegisteredVoiceHash(phone: String): String? {
        return null // placeholder for contract.getVoiceRecord(phone)
    }
}