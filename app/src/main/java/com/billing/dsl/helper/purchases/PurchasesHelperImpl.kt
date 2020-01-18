package com.billing.dsl.helper.purchases

import com.android.billingclient.api.BillingClient

internal class PurchasesHelperImpl : PurchasesHelper {

    override var billingClient: BillingClient? = null

    override fun hasPurchase(sku: String): Boolean {
        if (billingClient == null) {
            return false
        }

        val inApps = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
        val subscriptions = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)

        val purchases = inApps.purchasesList + subscriptions.purchasesList

        return purchases.any { it.sku == sku }
    }
}