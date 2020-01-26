package com.billing.dsl.logger

import android.util.Log

internal object Logger {

    var isEnabled = false

    private const val TAG = "Billing_DSL"

    fun log(msg: String) {
        if (isEnabled) {
            Log.d(TAG, msg)
        }
    }
}