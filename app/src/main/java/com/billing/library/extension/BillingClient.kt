package com.billing.library.extension

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun BillingClient.getInAppSkuDetails(skuList: List<String>) =
    suspendCoroutine<List<SkuDetails>> { continuation ->
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        querySkuDetailsAsync(params) { _, skuDetailsList ->
            continuation.resume(skuDetailsList)
        }
    }

suspend fun BillingClient.getSubscriptionSkuDetails(skuList: List<String>) =
    suspendCoroutine<List<SkuDetails>> { continuation ->
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()

        querySkuDetailsAsync(params) { _, skuDetailsList ->
            continuation.resume(skuDetailsList)
        }
    }