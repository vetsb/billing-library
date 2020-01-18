package com.billing.sample

import android.app.Application
import com.billing.dsl.BillingUtil

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        val configuration = BillingUtil.Configuration(this)
            .addSku("android.test.purchased")
            .setLoggingEnabled(true)

        BillingUtil.initialize(configuration)
    }
}