package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.*
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.vendor.ObjectConverter
import com.billing.dsl.vendor.waitUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class PurchaseFlowHelperImpl : PurchaseFlowHelper, CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    override var billingClient: BillingClient? = null

    private val channel = Channel<ResponseCode>(1)

    private var currentFlowSku: String? = null

    override suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        skuDetails: SkuDetails
    ): ResponseCode {
        if (!waitUntil { billingClient != null }) {
            return ResponseCode.ERROR
        }

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        currentFlowSku = skuDetails.sku

        billingClient!!.launchBillingFlow(activity, params)

        return channel.receive()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        launch {
            val responseCode = ObjectConverter.toLibraryResponseCode(
                billingResult?.responseCode
            )

            if (responseCode == ResponseCode.OK) {
                if (purchases == null || purchases.isEmpty()) {
                    channel.send(ResponseCode.ITEM_NOT_OWNED)

                    currentFlowSku = null

                    return@launch
                }

                val purchase = purchases.firstOrNull { it.sku == currentFlowSku }

                if (purchase == null) {
                    channel.send(ResponseCode.ITEM_NOT_OWNED)

                    currentFlowSku = null

                    return@launch
                }

                verifyPurchase(purchase)
            }

            currentFlowSku = null

            channel.send(responseCode)
        }
    }

    override suspend fun verifyPurchase(purchase: Purchase) {
        waitUntil { billingClient != null }

        if (billingClient == null) {
            return
        }

        if (purchase.isAcknowledged) {
            return
        }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient!!.acknowledgePurchase(params)
    }
}