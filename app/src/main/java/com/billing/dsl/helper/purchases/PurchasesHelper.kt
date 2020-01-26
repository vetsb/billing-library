package com.billing.dsl.helper.purchases

import com.android.billingclient.api.Purchase
import com.billing.dsl.helper.billing.BillingHelper

internal interface PurchasesHelper : BillingHelper {

    fun hasPurchase(sku: String): Boolean

    fun getPurchases(): List<Purchase>

    fun getPurchase(sku: String): Purchase?
}