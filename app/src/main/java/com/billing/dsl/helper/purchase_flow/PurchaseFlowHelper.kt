package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.billing.BillingHelper

internal interface PurchaseFlowHelper : BillingHelper, PurchasesUpdatedListener {

    suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        skuDetails: SkuDetails
    ): ResponseCode

    suspend fun verifyPurchase(purchase: Purchase)
}