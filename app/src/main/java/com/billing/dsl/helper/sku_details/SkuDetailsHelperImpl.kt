package com.billing.dsl.helper.sku_details

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.extension.getInAppSkuDetails
import com.billing.dsl.extension.getSubscriptionSkuDetails
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class SkuDetailsHelperImpl : SkuDetailsHelper {

    private var skuDetailsListDeferred: Deferred<List<SkuDetails>>? = null

    override var billingClient: BillingClient? = null

    override var isLoggingEnabled = false

    override suspend fun fetchSkuDetails(skuList: List<String>) {
        if (billingClient == null) {
            return
        }

        coroutineScope {
            skuDetailsListDeferred = async {
                val skuListCopy = ArrayList(skuList)

                val inAppSkuDetailsList = billingClient!!.getInAppSkuDetails(skuListCopy)
                val inAppSkuList = inAppSkuDetailsList.map { it.sku }

                skuListCopy.removeAll(inAppSkuList)

                val subsSkuDetailsList = billingClient!!.getSubscriptionSkuDetails(skuListCopy)

                (inAppSkuDetailsList + subsSkuDetailsList).distinctBy { it.sku }
            }
        }
    }

    override suspend fun getSkuDetails(): List<SkuDetails> {
        if (skuDetailsListDeferred == null) {
            return listOf()
        }

        return skuDetailsListDeferred!!.await()
    }
}