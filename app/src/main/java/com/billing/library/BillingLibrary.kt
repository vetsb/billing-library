package com.billing.library

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.*
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BillingLibrary : CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    private lateinit var globalBillingClient: BillingClient

    private lateinit var startConnectionDeferred: Deferred<Unit>

    private lateinit var skuDetailsListDeferred: Deferred<List<SkuDetails>>

    private val listeners = hashMapOf<String, ArrayList<PurchasesUpdatedListener>>()

    private val logger = BillingLibraryLogger()

    private var isInitialized = false

    fun initialize(application: Application, skuList: List<String>) {
        if (!isInitialized) {
            logger.log("BillingLibrary's initialization has started.")

            globalBillingClient = createGlobalBillingClient(application)

            startConnectionDeferred = async {
                try {
                    startBillingConnection()

                    logger.log("The connection to the billing client has started.")
                } catch (e: RuntimeException) {
                    logger.log(e.message)
                }
            }

            skuDetailsListDeferred = async {
                val skuDetailsList = fetchSkuDetailsList(skuList)

                logger.log("SkuDetailsList fetched from Google API. Result:")

                skuDetailsList.forEachIndexed { index, skuDetails ->
                    logger.log("${index + 1}. $skuDetails")
                }

                skuDetailsList
            }

            isInitialized = true

            logger.log("BillingLibrary is initialized.")
        }
    }

    fun addListenerBySku(sku: String, listener: PurchasesUpdatedListener) {
        checkInitialization()

        (listeners[sku] ?: arrayListOf()).remove(listener)

        logger.log("The listener (hashCode = ${listener.hashCode()}) is added to BillingLibrary by sku = $sku. There are ${listeners.values.flatten().size} listeners now.")
    }

    fun removeListener(listener: PurchasesUpdatedListener) {
        checkInitialization()

        listeners
            .filter { it.value.contains(listener) }
            .forEach {
                it.value.remove(listener)
            }

        logger.log("The listener (hashCode = ${listener.hashCode()}) is removed from BillingLibrary. There are ${listeners.values.flatten().size} listeners now.")
    }

    fun hasPurchase(sku: String, callback: ((value: Boolean) -> Unit)) {
        checkInitialization()

        CoroutineScope(Dispatchers.Main).launch {
            callback.invoke(hasPurchase(sku))
        }
    }

    suspend fun hasPurchase(sku: String): Boolean {
        checkInitialization()

        val skuDetails = skuDetailsListDeferred
            .await()
            .firstOrNull { it.sku == sku }

        if (skuDetails == null) {
            logger.log("SkuDetails with sku = $sku doesn't exist.")

            return false
        }

        val result = globalBillingClient
            .queryPurchases(skuDetails.type)
            ?.purchasesList
            ?.any { it.sku == sku }
            ?: false

        val message = if (result) {
            "SkuDetails with sku = $sku exists."
        } else {
            "SkuDetails with sku = $sku doesn't exist."
        }

        logger.log(message)

        return result
    }

    suspend fun startPurchaseFlow(activity: Activity?, sku: String) {
        logger.log("The purchase flow is started.")

        val skuDetails = skuDetailsListDeferred
            .await()
            .firstOrNull { it.sku == sku }

        if (skuDetails == null) {
            logger.log("SkuDetails with sku = $sku doesn't exist.")

            return
        }

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        globalBillingClient.launchBillingFlow(activity, params)
    }

    private fun createGlobalBillingClient(application: Application) =
        BillingClient.newBuilder(application)
            .setListener { _, purchases ->
                listeners.forEach { entry ->
                    val status = if (purchases?.any { it.sku == entry.key } == true)
                        PurchaseStatus.PURCHASED
                    else
                        PurchaseStatus.NOT_PURCHASED

                    entry.value.forEach { listener ->
                        listener.onUpdated(status)
                    }
                }
            }
            .enablePendingPurchases()
            .build()

    private suspend fun startBillingConnection() = suspendCoroutine<Unit> { continuation ->
        globalBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {}

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(RuntimeException("Billing Client couldn't connect to the server."))
                }
            }
        })
    }

    private suspend fun fetchSkuDetailsList(skuList: List<String>): List<SkuDetails> {
        if (!startConnectionDeferred.isCompleted) {
            startConnectionDeferred.await()
        }

        return globalBillingClient.getSubscriptionSkuDetails(skuList)
    }

    private fun checkInitialization() {
        if (!isInitialized) {
            val message = "BillingLibrary hasn't initialized yet."

            logger.log(message)

            throw RuntimeException(message)
        }
    }
}