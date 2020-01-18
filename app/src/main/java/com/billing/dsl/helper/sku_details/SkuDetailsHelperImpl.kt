package com.billing.dsl.helper.sku_details

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.extension.getInAppSkuDetails
import com.billing.dsl.extension.getSubscriptionSkuDetails
import com.billing.dsl.helper.purchase_flow.waitNotNullAndGet
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

internal class SkuDetailsHelperImpl : SkuDetailsHelper {

    private var skuDetailsListDeferred: Deferred<List<SkuDetails>>? = null

    override var billingClient: BillingClient? = null

    override suspend fun fetchSkuDetails(skuList: List<String>) {
        val client = waitNotNullAndGet(billingClient) ?: return

        coroutineScope {
            skuDetailsListDeferred = async {
                val skuListCopy = ArrayList(skuList)

                val inAppSkuDetailsList = client.getInAppSkuDetails(skuListCopy)
                val inAppSkuList = inAppSkuDetailsList.map { it.sku }

                skuListCopy.removeAll(inAppSkuList)

                val subsSkuDetailsList = client.getSubscriptionSkuDetails(skuListCopy)

                (inAppSkuDetailsList + subsSkuDetailsList).distinctBy { it.sku }
            }
        }
    }

    override suspend fun getSkuDetails(): List<SkuDetails> {
        val deferred = waitNotNullAndGet(skuDetailsListDeferred) ?: return listOf()

        return deferred.await()
    }
}