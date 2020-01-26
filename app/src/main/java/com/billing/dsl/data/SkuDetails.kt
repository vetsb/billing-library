package com.billing.dsl.data

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
)