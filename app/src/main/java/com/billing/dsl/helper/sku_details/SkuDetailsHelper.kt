package com.billing.dsl.helper.sku_details

import com.android.billingclient.api.SkuDetails
import com.billing.dsl.helper.billing.BillingHelper

internal interface SkuDetailsHelper : BillingHelper {

    suspend fun fetchSkuDetails(skuList: List<String>)

    suspend fun getSkuDetails(sku: String): SkuDetails?

    suspend fun getSkuDetailsList(): List<SkuDetails>

    suspend fun getSkuList(): List<String>
}