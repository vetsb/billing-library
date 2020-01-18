package com.billing.dsl.helper.billing

import com.android.billingclient.api.BillingClient

internal interface BillingHelper {

    var billingClient: BillingClient?
}