package com.billing.library

interface PurchasesUpdatedListener {

    fun onUpdated(status: PurchaseStatus)
}