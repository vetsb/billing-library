package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.*
import com.billing.dsl.data.PurchaseFlowResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal inline fun <reified T> waitNotNullAndGet(obj: T?): T? {
    val timeoutMillis = 5000
    val startTime = System.currentTimeMillis()

    while (obj == null) {
        val currentTime = System.currentTimeMillis()

        if (currentTime - startTime >= timeoutMillis) {
            break
        } else {
            continue
        }
    }

    return obj
}

class PurchaseFlowHelperImpl : PurchaseFlowHelper, CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    override var billingClient: BillingClient? = null

    private val channel = Channel<PurchaseFlowResult>(1)

    private var currentFlowSku: String? = null

    override suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        skuDetails: SkuDetails
    ): PurchaseFlowResult {
        val client = waitNotNullAndGet(billingClient) ?: return PurchaseFlowResult.ERROR

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        client.launchBillingFlow(activity, params)

        return channel.receive()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        launch {
            when (billingResult?.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (purchases == null || purchases.isEmpty()) {
                        channel.send(PurchaseFlowResult.ERROR)

                        return@launch
                    }

                    val purchase = purchases.firstOrNull { it.sku == currentFlowSku }

                    if (purchase == null) {
                        channel.send(PurchaseFlowResult.ERROR)

                        return@launch
                    }

                    verifyPurchase(purchase)

                    channel.send(PurchaseFlowResult.SUCCESS)
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    channel.send(PurchaseFlowResult.CANCELLED)
                }
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                    channel.send(PurchaseFlowResult.ALREADY_HAVE)
                }
                else -> {
                    channel.send(PurchaseFlowResult.ERROR)
                }
            }
        }
    }

    override suspend fun verifyPurchase(purchase: Purchase) {
        if (purchase.isAcknowledged) {
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient?.acknowledgePurchase(params)
    }
}