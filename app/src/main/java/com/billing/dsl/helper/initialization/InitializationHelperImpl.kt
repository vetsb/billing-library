package com.billing.dsl.helper.initialization

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.billing.dsl.data.ResponseCode
import com.billing.dsl.vendor.ObjectConverter
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class InitializationHelperImpl : InitializationHelper {

    override var billingClient: BillingClient? = null

    private val listeners = arrayListOf<PurchasesUpdatedListener>()

    override fun addListener(listener: PurchasesUpdatedListener) {
        listeners.add(listener)
    }

    override suspend fun initialize(context: Context) =
        suspendCoroutine<ResponseCode> { continuation ->
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
                    continuation.resume(
                        ObjectConverter.toLibraryResponseCode(
                            result?.responseCode
                        )
                    )
                }
            })

        }
}