package com.billing.dsl.helper.purchases

import com.billing.dsl.helper.billing.BillingHelper

internal interface PurchasesHelper : BillingHelper {

    fun hasPurchase(sku: String): Boolean
}