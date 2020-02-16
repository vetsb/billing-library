package com.billing.dsl.data

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.constant.FreeTrialPeriod
import com.billing.dsl.constant.SkuType
import com.billing.dsl.constant.SubscriptionPeriod

data class SkuDetails(
    val description: String?,
    val freeTrialPeriod: FreeTrialPeriod?,
    val iconUrl: String?,

    val introductoryPrice: String?,
    val introductoryPriceAmountMicros: Long?,
    val introductoryPriceCycles: String?,
    val introductoryPricePeriod: String?,

    val originalJson: String?,
    val originalPrice: String?,
    val originalPriceAmountMicros: Long?,

    val price: String?,
    val priceAmountMicros: Long?,
    val priceCurrencyCode: String?,

    val sku: String?,
    val subscriptionPeriod: SubscriptionPeriod?,
    val title: String?,
    val type: SkuType?,
    val isRewarded: Boolean?
) {

    companion object {
        internal fun fromGoogleSkuDetails(skuDetails: SkuDetails?): com.billing.dsl.data.SkuDetails? {
            if (skuDetails == null) {
                return null
            }

            return skuDetails.run {
                SkuDetails(
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
            }
        }
    }
}