package com.billing.dsl.vendor

internal fun waitUntil(
    require: () -> Boolean
): Boolean {
    val timeoutMillis = 5000
    val startTime = System.currentTimeMillis()

    while (!require()) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - startTime >= timeoutMillis) {
            return false
        } else {
            continue
        }
    }

    return true
}