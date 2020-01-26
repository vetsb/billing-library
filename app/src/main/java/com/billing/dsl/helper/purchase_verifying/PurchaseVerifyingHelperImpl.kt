package com.billing.dsl.helper.purchase_verifying

import com.android.billingclient.api.*
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.logger.Logger
import com.billing.dsl.vendor.waitUntil

internal class PurchaseVerifyingHelperImpl(
    private val skuDetailsHelper: SkuDetailsHelper
) : PurchaseVerifyingHelper {

    override var billingClient: BillingClient? = null

    override suspend fun verify(purchase: Purchase) {
        Logger.log("Purchase verifying. Started. Purchase = $purchase")

        if (!waitUntil { billingClient != null }) {
            Logger.log("Purchase verifying. Failed. BillingClient isn't ready")

            return
        }

        if (purchase.isAcknowledged) {
            Logger.log("Purchase verifying. Purchase is acknowledged already")

            return
        }

        val skuDetails = skuDetailsHelper.getSkuDetails(purchase.sku)

        when (skuDetails?.type) {
            BillingClient.SkuType.INAPP -> {
                val params = ConsumeParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient!!.consumePurchase(params)

                Logger.log("Purchase verifying. In-App purchase is verified")
            }
            BillingClient.SkuType.SUBS -> {
                val params = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()

                billingClient!!.acknowledgePurchase(params)

                Logger.log("Purchase verifying. Subscription purchase is verified")
            }
        }
    }
}