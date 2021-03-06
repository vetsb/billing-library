package com.billing.dsl.vendor

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.billing.dsl.constant.FreeTrialPeriod
import com.billing.dsl.constant.SkuType
import com.billing.dsl.constant.SubscriptionPeriod
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal suspend fun BillingClient.getInAppSkuDetails(skuList: List<String>) =
    suspendCoroutine<List<SkuDetails>> { continuation ->
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.INAPP)
            .build()

        querySkuDetailsAsync(params) { _, skuDetailsList ->
            continuation.resume(skuDetailsList)
        }
    }

internal suspend fun BillingClient.getSubscriptionSkuDetails(skuList: List<String>) =
    suspendCoroutine<List<SkuDetails>> { continuation ->
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(skuList)
            .setType(BillingClient.SkuType.SUBS)
            .build()

        querySkuDetailsAsync(params) { _, skuDetailsList ->
            continuation.resume(skuDetailsList)
        }
    }

internal fun SkuDetails.toLibraryInstance() = com.billing.dsl.data.SkuDetails(
    description = description,
    freeTrialPeriod = when (freeTrialPeriod) {
        "P3D" -> FreeTrialPeriod.THREE_DAYS
        "P1W" -> FreeTrialPeriod.WEEK
        "P2W" -> FreeTrialPeriod.TWO_WEEK
        "P1M" -> FreeTrialPeriod.MONTH
        else -> null
    },
    iconUrl = iconUrl,

    introductoryPrice = introductoryPrice,
    introductoryPriceAmountMicros = introductoryPriceAmountMicros,
    introductoryPriceCycles = introductoryPriceCycles,
    introductoryPricePeriod = introductoryPricePeriod,

    originalJson = originalJson,
    originalPrice = originalPrice,
    originalPriceAmountMicros = originalPriceAmountMicros,

    price = price,
    priceAmountMicros = priceAmountMicros,
    priceCurrencyCode = priceCurrencyCode,

    sku = sku,
    subscriptionPeriod = when (subscriptionPeriod) {
        "P1W" -> SubscriptionPeriod.WEEK
        "P1M" -> SubscriptionPeriod.MONTH
        "P3M" -> SubscriptionPeriod.THREE_MONTHS
        "P6M" -> SubscriptionPeriod.SIX_MONTHS
        "P1Y" -> SubscriptionPeriod.YEAR
        else -> null
    },
    title = title,
    type = when (type) {
        BillingClient.SkuType.INAPP -> SkuType.INAPP
        BillingClient.SkuType.SUBS -> SkuType.SUBS
        else -> null
    },
    isRewarded = isRewarded
)

internal fun Purchase.toLibraryInstance() = com.billing.dsl.data.Purchase(
    developerPayload = developerPayload,
    orderId = orderId,
    originalJson = originalJson,
    packageName = packageName,
    purchaseState = purchaseState,
    purchaseTime = purchaseTime,
    purchaseToken = purchaseToken,
    signature = signature,
    sku = sku,
    isAcknowledged = isAcknowledged,
    isAutoRenewing = isAutoRenewing
)