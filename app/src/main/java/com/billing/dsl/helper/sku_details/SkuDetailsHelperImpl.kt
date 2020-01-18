package com.billing.dsl.helper.sku_details

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.vendor.getInAppSkuDetails
import com.billing.dsl.vendor.getSubscriptionSkuDetails
import com.billing.dsl.vendor.waitUntil
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class SkuDetailsHelperImpl : SkuDetailsHelper {

    private var skuDetailsListDeferred: Deferred<List<SkuDetails>>? = null

    private var initialSkuList: List<String>? = null

    override var billingClient: BillingClient? = null

    override suspend fun fetchSkuDetails(skuList: List<String>) {
        coroutineScope {
            skuDetailsListDeferred = async {
                initialSkuList = ArrayList(skuList)

                val skuListCopy = ArrayList(skuList)

                val inAppSkuDetailsList = billingClient!!.getInAppSkuDetails(skuListCopy)
                val inAppSkuList = inAppSkuDetailsList.map { it.sku }

                skuListCopy.removeAll(inAppSkuList)

                val subsSkuDetailsList = billingClient!!.getSubscriptionSkuDetails(skuListCopy)

                (inAppSkuDetailsList + subsSkuDetailsList).distinctBy { it.sku }
            }
        }
    }

    override suspend fun getSkuDetails(sku: String): SkuDetails? {
        return getSkuDetailsList().firstOrNull { it.sku == sku }
    }

    override suspend fun getSkuDetailsList(): List<SkuDetails> {
        waitUntil { skuDetailsListDeferred != null }

        if (skuDetailsListDeferred == null) {
            return listOf()
        }

        return skuDetailsListDeferred!!.await()
    }

    override suspend fun getSkuList(): List<String> {
        waitUntil { skuDetailsListDeferred != null }

        if (skuDetailsListDeferred == null) {
            return listOf()
        }

        return skuDetailsListDeferred!!.await().map { it.sku }
    }
}