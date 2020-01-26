package com.billing.dsl.helper.initialization

import android.content.Context
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.logger.Logger
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
            Logger.log("BillingClient started initialization")

            billingClient = BillingClient.newBuilder(context.applicationContext)
                .setListener { billingResult, purchases ->
                    val responseCode =
                        ObjectConverter.toLibraryResponseCode(billingResult?.responseCode)

                    Logger.log("BillingClient received callback with responseCode = $responseCode and purchases = $purchases")

                    listeners.forEach {
                        it.onPurchasesUpdated(billingResult, purchases)
                    }
                }
                .enablePendingPurchases()
                .build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingServiceDisconnected() {
                    Logger.log("BillingClient disconnected")
                }

                override fun onBillingSetupFinished(result: BillingResult?) {
                    val responseCode = ObjectConverter.toLibraryResponseCode(result?.responseCode)

                    Logger.log("BillingClient finished setup with responseCode = $responseCode")

                    continuation.resume(
                        ObjectConverter.toLibraryResponseCode(
                            result?.responseCode
                        )
                    )
                }
            })

        }
}