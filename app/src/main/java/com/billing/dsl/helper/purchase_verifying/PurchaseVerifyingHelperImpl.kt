package com.billing.dsl.helper.purchase_verifying

import com.android.billingclient.api.*
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.vendor.waitUntil

internal class PurchaseVerifyingHelperImpl(
    private val skuDetailsHelper: SkuDetailsHelper
) : PurchaseVerifyingHelper {

    override var billingClient: BillingClient? = null

    override suspend fun verify(purchase: Purchase) {
        if (!waitUntil { billingClient != null }) {
            return
        }

        if (purchase.isAcknowledged) {
            return
        }

        val skuDetails = skuDetailsHelper.getSkuDetails(purchase.sku)

        when (skuDetails?.type) {
            BillingClient.SkuType.INAPP -> {
                val params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient!!.consumePurchase(params)
            }
            BillingClient.SkuType.SUBS -> {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient!!.acknowledgePurchase(params)
            }
        }


    }
}