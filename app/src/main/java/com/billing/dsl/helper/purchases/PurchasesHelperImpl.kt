package com.billing.dsl.helper.purchases

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.billing.dsl.vendor.waitUntil

internal class PurchasesHelperImpl : PurchasesHelper {

    override var billingClient: BillingClient? = null

    override fun hasPurchase(sku: String): Boolean {
        if (!waitUntil { billingClient != null }) {
            return false
        }

        return getPurchases().any { it.sku == sku }
    }

    override fun getPurchases(): List<Purchase> {
        if (!waitUntil { billingClient != null }) {
            return listOf()
        }

        val inApps = billingClient!!.queryPurchases(BillingClient.SkuType.INAPP)
        val subscriptions = billingClient!!.queryPurchases(BillingClient.SkuType.SUBS)

        return inApps.purchasesList + subscriptions.purchasesList
    }

    override fun getPurchase(sku: String): Purchase? {
        if (!waitUntil { billingClient != null }) {
            return null
        }

        return getPurchases()
            .firstOrNull { it.sku == sku }
    }
}