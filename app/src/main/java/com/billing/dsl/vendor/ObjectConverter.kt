package com.billing.dsl.vendor

import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.billing.dsl.data.ResponseCode

internal object ObjectConverter {

    fun toLibraryResponseCode(code: Int?) = when (code) {
        SERVICE_TIMEOUT -> ResponseCode.SERVICE_TIMEOUT
        FEATURE_NOT_SUPPORTED -> ResponseCode.FEATURE_NOT_SUPPORTED
        SERVICE_DISCONNECTED -> ResponseCode.SERVICE_DISCONNECTED
        OK -> ResponseCode.OK
        USER_CANCELED -> ResponseCode.USER_CANCELED
        SERVICE_UNAVAILABLE -> ResponseCode.SERVICE_UNAVAILABLE
        BILLING_UNAVAILABLE -> ResponseCode.BILLING_UNAVAILABLE
        ITEM_UNAVAILABLE -> ResponseCode.ITEM_UNAVAILABLE
        DEVELOPER_ERROR -> ResponseCode.DEVELOPER_ERROR
        ERROR -> ResponseCode.ERROR
        ITEM_ALREADY_OWNED -> ResponseCode.ITEM_ALREADY_OWNED
        ITEM_NOT_OWNED -> ResponseCode.ITEM_NOT_OWNED
        else -> ResponseCode.UNDEFINED
    }
}