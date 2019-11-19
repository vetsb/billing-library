package com.billing.library

import android.util.Log

class BillingLibraryLogger {

    var isLoggingEnabled = BuildConfig.DEBUG

    companion object {
        private const val TAG = "BillingLibrary"
    }

    fun log(any: Any?) {
        if (isLoggingEnabled) {
            Log.d(TAG, any.toString())
        }
    }
}