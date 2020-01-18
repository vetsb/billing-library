package com.billing.dsl.data

data class Purchase(
    val developerPayload: String?,
    val orderId: String?,
    val originalJson: String?,
    val packageName: String?,
    val purchaseState: Int?,
    val purchaseTime: Long?,
    val purchaseToken: String?,
    val signature: String?,
    val sku: String?,
    val isAcknowledged: Boolean?,
    val isAutoRenewing: Boolean?
)