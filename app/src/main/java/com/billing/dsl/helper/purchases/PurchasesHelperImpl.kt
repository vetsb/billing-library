package com.billing.dsl.helper.purchases

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.billing.dsl.vendor.waitUntil

internal class PurchasesHelperImpl : PurchasesHelper {

    override var billingClient: BillingClient? = null

    override fun hasPurchase(sku: String): Boolean {
        waitUntil { billingClient != null }

        return getPurchases().any { it.sku == sku }
    }

    override fun getPurchases(): List<Purchase> {
        waitUntil { billingClient != null }

        val inApps = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
        val subscriptions = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)

        return inApps.purchasesList + subscriptions.purchasesList
    }

    override fun getPurchase(sku: String): Purchase? {
        waitUntil { billingClient != null }

        return getPurchases()
            .firstOrNull { it.sku == sku }
    }
}