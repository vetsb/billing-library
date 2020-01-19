package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.*
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.purchase_verifying.PurchaseVerifyingHelper
import com.billing.dsl.vendor.ObjectConverter
import com.billing.dsl.vendor.waitUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

internal class PurchaseFlowHelperImpl(
    private val purchaseVerifyingHelper: PurchaseVerifyingHelper
) : PurchaseFlowHelper, CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    override var billingClient: BillingClient? = null

    private var flowChannel: Channel<ResponseCode>? = null

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

        flowChannel = Channel(1)

        billingClient!!.launchBillingFlow(activity, params)

        return flowChannel!!.receive()
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
                    finishPurchaseHandling(ResponseCode.ITEM_NOT_OWNED)

                    return@launch
                }

                val purchase = purchases.firstOrNull { it.sku == currentFlowSku }

                if (purchase == null) {
                    finishPurchaseHandling(ResponseCode.ITEM_NOT_OWNED)

                    return@launch
                }

                purchaseVerifyingHelper.verify(purchase)
            }

            finishPurchaseHandling(responseCode)
        }
    }

    private suspend fun finishPurchaseHandling(responseCode: ResponseCode) {
        currentFlowSku = null

        flowChannel?.send(responseCode)
    }
}