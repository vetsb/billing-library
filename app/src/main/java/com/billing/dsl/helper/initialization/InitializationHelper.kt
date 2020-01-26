package com.billing.dsl.helper.initialization

import android.content.Context
import com.android.billingclient.api.PurchasesUpdatedListener
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.billing.BillingHelper

internal interface InitializationHelper : BillingHelper {

    fun addListener(listener: PurchasesUpdatedListener)

    suspend fun initialize(context: Context): ResponseCode
}