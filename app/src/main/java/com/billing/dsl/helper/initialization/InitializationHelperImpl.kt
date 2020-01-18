package com.billing.dsl.helper.initialization

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.billing.dsl.data.ConnectionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InitializationHelperImpl : InitializationHelper, CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    override var billingClient: BillingClient? = null

    private val listeners = arrayListOf<PurchasesUpdatedListener>()

    override fun addListener(listener: PurchasesUpdatedListener) {
        listeners.add(listener)
    }

    override suspend fun initialize(context: Context) =
        suspendCoroutine<ConnectionResult> { continuation ->
            billingClient = BillingClient.newBuilder(context.applicationContext)
                .setListener { billingResult, purchases ->
                    listeners.forEach {
                        it.onPurchasesUpdated(billingResult, purchases)
                    }
                }
                .enablePendingPurchases()
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {

                }

                override fun onBillingSetupFinished(result: BillingResult?) {
                    val connectionResult = when (result?.responseCode) {
                        BillingClient.BillingResponseCode.OK -> ConnectionResult.SUCCESS
                        else -> ConnectionResult.FAILURE
                    }

                    continuation.resume(connectionResult)
                }
            })

        }
}