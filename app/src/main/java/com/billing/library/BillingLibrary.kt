package com.billing.library

import android.app.Activity
import android.app.Application
import com.android.billingclient.api.*
import com.billing.library.constant.ProductType
import com.billing.library.constant.PurchaseStatus
import com.billing.library.extension.getInAppSkuDetails
import com.billing.library.extension.getSubscriptionSkuDetails
import com.billing.library.listener.PurchaseStatusUpdatedListener
import com.billing.library.logger.BillingLibraryLogger
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object BillingLibrary : CoroutineScope {

    override val coroutineContext = SupervisorJob() + Dispatchers.IO

    private lateinit var globalBillingClient: BillingClient

    private lateinit var startConnectionDeferred: Deferred<Unit>

    private lateinit var skuDetailsListDeferred: Deferred<List<SkuDetails>>

    private val listeners = hashMapOf<String, ArrayList<PurchaseStatusUpdatedListener>>()

    private val logger = BillingLibraryLogger()

    private var isInitialized = false

    var isLoggingEnabled = BuildConfig.DEBUG
        set(value) {
            field = value

            logger.isLoggingEnabled = value
        }

    fun initialize(application: Application, skuToType: Map<String, ProductType>) {
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
                val skuDetailsList = fetchSkuDetailsList(skuToType)

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

    fun addSku(sku: String, type: ProductType) {
        checkInitialization()

        logger.log("addSku. Adding new sku is started.")

        skuDetailsListDeferred = async {
            val new = when (type) {
                ProductType.SUBS -> globalBillingClient.getSubscriptionSkuDetails(listOf(sku))
                ProductType.INAPP -> globalBillingClient.getInAppSkuDetails(listOf(sku))
            }

            logger.log("addSku. New SkuDetails is fetched. Result = $new")

            skuDetailsListDeferred.await() + new
        }
    }

    fun addSku(skuToType: Map<String, ProductType>) {
        checkInitialization()

        logger.log("addSku. Adding new skuList is started.")

        skuDetailsListDeferred = async {
            val inAppSkuDetails = globalBillingClient.getInAppSkuDetails(skuToType
                .filter { it.value == ProductType.INAPP }
                .keys
                .toList())

            val subsSkuDetails = globalBillingClient.getSubscriptionSkuDetails(skuToType
                .filter { it.value == ProductType.SUBS }
                .keys
                .toList())

            val new = inAppSkuDetails + subsSkuDetails

            logger.log("addSku. New SkuDetails is fetched. Result = $new")

            skuDetailsListDeferred.await() + new
        }
    }

    fun addListenerBySku(sku: String, listener: PurchaseStatusUpdatedListener) {
        checkInitialization()

        (listeners[sku] ?: arrayListOf()).remove(listener)

        logger.log("addListenerBySku. The listener (hashCode = ${listener.hashCode()}) is added to BillingLibrary by sku = $sku. There are ${listeners.values.flatten().size} listeners now.")
    }

    fun removeListener(listener: PurchaseStatusUpdatedListener) {
        checkInitialization()

        listeners
            .filter { it.value.contains(listener) }
            .forEach {
                it.value.remove(listener)
            }

        logger.log("removeListener. The listener (hashCode = ${listener.hashCode()}) is removed from BillingLibrary. There are ${listeners.values.flatten().size} listeners now.")
    }

    suspend fun hasPurchase(sku: String): Boolean {
        checkInitialization()

        val skuDetails = skuDetailsListDeferred
            .await()
            .firstOrNull { it.sku == sku }

        if (skuDetails == null) {
            logger.log("hasPurchase. SkuDetails with sku = $sku doesn't exist.")

            return false
        }

        val result = globalBillingClient
            .queryPurchases(skuDetails.type)
            ?.purchasesList
            ?.any { it.sku == sku }
            ?: false

        val message = if (result) {
            "hasPurchase. SkuDetails with sku = $sku exists."
        } else {
            "hasPurchase. SkuDetails with sku = $sku doesn't exist."
        }

        logger.log(message)

        return result
    }

    @Throws(RuntimeException::class)
    suspend fun startPurchaseFlow(activity: Activity?, sku: String) {
        checkInitialization()

        logger.log("The purchase flow is started.")

        val skuDetails = skuDetailsListDeferred
            .await()
            .firstOrNull { it.sku == sku }

        if (skuDetails == null) {
            logger.log("startPurchaseFlow. SkuDetails with sku = $sku doesn't exist.")

            return
        } else {
            logger.log("startPurchaseFlow. SkuDetails with sku = $sku exists.")
        }

        val params = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()

        val billingResult = globalBillingClient.launchBillingFlow(activity, params)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            logger.log("startPurchaseFlow. The billing flow has launched successfully.")
        } else {
            val message = "startPurchaseFlow. The launching of the billing flow went wrong."

            logger.log(message)

            throw RuntimeException(message)
        }
    }

    suspend fun getSkuDetails(sku: String): SkuDetails? {
        checkInitialization()

        val skuDetails = skuDetailsListDeferred.await()
            .firstOrNull { it.sku == sku }

        if (skuDetails == null) {
            logger.log("getSkuDetails. SkuDetails with sku = $sku doesn't exist.")
        } else {
            logger.log("getSkuDetails. SkuDetails with sku = $sku exists.")
        }

        return skuDetails
    }

    private fun createGlobalBillingClient(application: Application) =
        BillingClient.newBuilder(application)
            .setListener { _, purchases ->
                logger.log("Billing Listener is triggered.")

                listeners.forEach { entry ->
                    val status = if (purchases?.any { it.sku == entry.key } == true)
                        PurchaseStatus.PURCHASED
                    else
                        PurchaseStatus.NOT_PURCHASED

                    logger.log("Billing Listener. The purchase with sku = ${entry.key} has status = $status.")

                    entry.value.forEach { listener ->
                        listener.onUpdated(status)

                        logger.log("Billing Listener. ")
                    }
                }
            }
            .enablePendingPurchases()
            .build()

    private suspend fun startBillingConnection() = suspendCoroutine<Unit> { continuation ->
        globalBillingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                logger.log("The Billing Client disconnected.")
            }

            override fun onBillingSetupFinished(billingResult: BillingResult?) {
                if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                    continuation.resume(Unit)
                } else {
                    continuation.resumeWithException(RuntimeException("Billing Client couldn't connect to the server."))
                }
            }
        })
    }

    private suspend fun fetchSkuDetailsList(skuToType: Map<String, ProductType>): List<SkuDetails> {
        startConnectionDeferred.await()

        val inAppSkuDetails = globalBillingClient.getInAppSkuDetails(skuToType
            .filter { it.value == ProductType.INAPP }
            .keys
            .toList())

        val subsSkuDetails = globalBillingClient.getSubscriptionSkuDetails(skuToType
            .filter { it.value == ProductType.SUBS }
            .keys
            .toList())

        return inAppSkuDetails + subsSkuDetails
    }

    @Throws(RuntimeException::class)
    private fun checkInitialization() {
        if (!isInitialized) {
            val message = "BillingLibrary hasn't initialized yet."

            logger.log(message)

            throw RuntimeException(message)
        }
    }
}