package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import com.android.billingclient.api.PurchasesUpdatedListener
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.billing.BillingHelper

internal interface PurchaseFlowHelper : BillingHelper, PurchasesUpdatedListener {

    var isAcknowledgeEnabled: Boolean

    suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        sku: String
    ): ResponseCode
}