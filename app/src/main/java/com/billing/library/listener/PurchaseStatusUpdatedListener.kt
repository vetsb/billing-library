package com.billing.library.listener

import com.billing.library.constant.PurchaseStatus

interface PurchaseStatusUpdatedListener {

    fun onUpdated(status: PurchaseStatus)
}