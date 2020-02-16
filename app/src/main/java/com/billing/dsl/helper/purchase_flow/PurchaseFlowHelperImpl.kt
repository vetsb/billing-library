package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.purchase_verifying.PurchaseVerifyingHelper
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.logger.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class PurchaseFlowHelperImpl(
    private val purchaseVerifyingHelper: PurchaseVerifyingHelper,
    private val skuDetailsHelper: SkuDetailsHelper
) : PurchaseFlowHelper, CoroutineScope {

    override var isAcknowledgeEnabled = true

    override val coroutineContext = Job() + Dispatchers.IO

    override var billingClient: BillingClient? = null

    private var flowChannel: Channel<ResponseCode>? = null

    private var currentFlowSku: String? = null

    override suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        sku: String
    ): ResponseCode {
        Logger.log("PurchaseFlow. Started")

        val skuDetails = skuDetailsHelper.getSkuDetails(sku)

        if (skuDetails == null) {
            Logger.log("PurchaseFlow. Failed. SkuDetails is null")

            return ResponseCode.ERROR
        }

        currentFlowSku = skuDetails.sku

        flowChannel = Channel(1)

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        Logger.log("PurchaseFlow. Dialog is opened")

        billingClient!!.launchBillingFlow(activity, params)

        return flowChannel!!.receive()
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchases: MutableList<Purchase>?
    ) {
        launch {
            val responseCode = ResponseCode.fromGoogleResponseCode(billingResult?.responseCode)

            Logger.log("PurchaseFlow. Received callback with responseCode = $responseCode and purchases = $purchases")

            if (responseCode == ResponseCode.OK) {
                if (purchases == null || purchases.isEmpty()) {
                    Logger.log("PurchaseFlow. Failed. Purchases is empty")

                    finishPurchaseHandling(ResponseCode.ITEM_NOT_OWNED)

                    return@launch
                }

                val purchase = purchases.firstOrNull { it.sku == currentFlowSku }

                if (purchase == null) {
                    Logger.log("PurchaseFlow. Failed. Purchase with sku = $currentFlowSku is null")

                    finishPurchaseHandling(ResponseCode.ITEM_NOT_OWNED)

                    return@launch
                }

                launch {
                    if (isAcknowledgeEnabled) {
                        purchaseVerifyingHelper.verify(purchase)
                    }
                }
            }

            Logger.log("PurchaseFlow. ResponseCode = $responseCode")

            finishPurchaseHandling(responseCode)
        }
    }

    private suspend fun finishPurchaseHandling(responseCode: ResponseCode) {
        currentFlowSku = null

        flowChannel?.send(responseCode)
    }
}