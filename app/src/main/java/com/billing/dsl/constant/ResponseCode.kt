package com.billing.dsl.constant

import com.android.billingclient.api.BillingClient

enum class ResponseCode {

    BILLING_UNAVAILABLE,
    DEVELOPER_ERROR,
    ERROR,
    FEATURE_NOT_SUPPORTED,
    ITEM_ALREADY_OWNED,
    ITEM_NOT_OWNED,
    ITEM_UNAVAILABLE,
    OK,
    SERVICE_DISCONNECTED,
    SERVICE_TIMEOUT,
    SERVICE_UNAVAILABLE,
    USER_CANCELED,
    UNDEFINED;

    companion object {
        fun fromGoogleResponseCode(responseCode: Int?): ResponseCode {
            return when (responseCode) {
                BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> SERVICE_TIMEOUT
                BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> FEATURE_NOT_SUPPORTED
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> SERVICE_DISCONNECTED
                BillingClient.BillingResponseCode.OK -> OK
                BillingClient.BillingResponseCode.USER_CANCELED -> USER_CANCELED
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> BILLING_UNAVAILABLE
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> ITEM_UNAVAILABLE
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> DEVELOPER_ERROR
                BillingClient.BillingResponseCode.ERROR -> ERROR
                BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> ITEM_ALREADY_OWNED
                BillingClient.BillingResponseCode.ITEM_NOT_OWNED -> ITEM_NOT_OWNED
                else -> UNDEFINED
            }
        }
    }
}