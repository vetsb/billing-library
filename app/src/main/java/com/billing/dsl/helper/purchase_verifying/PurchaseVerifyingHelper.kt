package com.billing.dsl.helper.purchase_verifying

import com.android.billingclient.api.Purchase
import com.billing.dsl.helper.billing.BillingHelper

internal interface PurchaseVerifyingHelper : BillingHelper {

    suspend fun verify(purchase: Purchase)
}