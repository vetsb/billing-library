package com.billing.library.logger

import android.util.Log
import com.billing.library.BuildConfig

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