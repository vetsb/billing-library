package com.billing.dsl.data

data class SkuDetails(
    val description: String?,
    val freeTrialPeriod: String?,
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
    val subscriptionPeriod: String?,
    val title: String?,
    val type: String?,
    val isRewarded: Boolean?
)