package com.billing.dsl.data

import com.android.billingclient.api.Purchase

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
) {

    companion object {
        fun fromGooglePurchase(purchase: Purchase?): com.billing.dsl.data.Purchase? {
            if (purchase == null) {
                return null
            }

            return purchase.run {
                Purchase(
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
            }
        }
    }
}