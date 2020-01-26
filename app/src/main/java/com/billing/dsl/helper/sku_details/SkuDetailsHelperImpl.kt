package com.billing.dsl.helper.sku_details

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.vendor.getInAppSkuDetails
import com.billing.dsl.vendor.getSubscriptionSkuDetails
import com.billing.dsl.vendor.waitUntil
import kotlinx.coroutines.*

internal class SkuDetailsHelperImpl : SkuDetailsHelper, CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    private var skuDetailsListDeferred: Deferred<List<SkuDetails>>? = null

    private var initialSkuList: List<String>? = null

    override var billingClient: BillingClient? = null

    override fun fetchSkuDetails(skuList: List<String>) {
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

    override suspend fun getSkuDetails(sku: String): SkuDetails? {
        return getSkuDetailsList().firstOrNull { it.sku == sku }
    }

    override suspend fun getSkuDetailsList(): List<SkuDetails> {
        if (!waitUntil { skuDetailsListDeferred != null }) {
            return listOf()
        }

        return skuDetailsListDeferred!!.await()
    }

    override fun getSkuList(): List<String> {
        if (!waitUntil { initialSkuList != null }) {
            return listOf()
        }

        return initialSkuList!!
    }
}