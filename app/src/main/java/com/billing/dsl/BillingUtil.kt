package com.billing.dsl

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.data.ResponseCode
import com.billing.dsl.helper.initialization.InitializationHelper
import com.billing.dsl.helper.initialization.InitializationHelperImpl
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelper
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelperImpl
import com.billing.dsl.helper.purchases.PurchasesHelper
import com.billing.dsl.helper.purchases.PurchasesHelperImpl
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.helper.sku_details.SkuDetailsHelperImpl
import com.billing.dsl.logger.Logger
import com.billing.dsl.vendor.toLibraryInstance
import com.billing.dsl.vendor.waitUntil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object BillingUtil : CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.IO

    private val initializationHelper: InitializationHelper by lazy {
        InitializationHelperImpl()
    }

    private val skuDetailsHelper: SkuDetailsHelper by lazy {
        SkuDetailsHelperImpl()
    }

    private val purchaseFlowHelper: PurchaseFlowHelper by lazy {
        PurchaseFlowHelperImpl()
    }

    private val purchasesHelper: PurchasesHelper by lazy {
        PurchasesHelperImpl()
    }

    private var isInitialized = false

    class Configuration(
        val context: Context
    ) {

        internal var skuList = arrayListOf<String>()

        internal var isLoggingEnabled = false

        fun setLoggingEnabled(value: Boolean): Configuration {
            isLoggingEnabled = value

            return this
        }

        fun addSku(sku: String): Configuration {
            skuList.add(sku)

            return this
        }

        fun addSkuList(list: List<String>): Configuration {
            skuList.addAll(list)

            return this
        }
    }

    fun initialize(configuration: Configuration) {
        check(!isInitialized) { "BillingUtil has been initialized already" }

        Logger.isEnabled = configuration.isLoggingEnabled

        launch(Dispatchers.Main) {
            initializationHelper.addListener(purchaseFlowHelper)

            val connectionResult = initializationHelper.initialize(configuration.context)

            Logger.log(
                when (connectionResult) {
                    ResponseCode.OK -> "Billing Client is connected"
                    else -> "Billing Client isn't connected. Error = ${connectionResult.name}"
                }
            )

            listOf(skuDetailsHelper, purchaseFlowHelper, purchasesHelper).forEach {
                it.billingClient = initializationHelper.billingClient
            }

            skuDetailsHelper.run {
                fetchSkuDetails(configuration.skuList)
            }
        }

        isInitialized = true
    }

    fun hasPurchase(sku: String): Boolean {
        waitUntil { initializationHelper.billingClient != null && initializationHelper.billingClient?.isReady == false }

        return purchasesHelper.hasPurchase(sku)
    }

    suspend fun getSkuList(): List<String> {
        waitUntil { initializationHelper.billingClient != null && initializationHelper.billingClient?.isReady == false }

        return skuDetailsHelper.getSkuList()
    }

    suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        sku: String
    ): ResponseCode {
        val skuDetails = skuDetailsHelper
            .getSkuDetailsList()
            .firstOrNull { it.sku == sku }
            ?: return ResponseCode.ERROR

        return purchaseFlowHelper.startPurchaseFlowAndGetResult(activity, skuDetails)
    }

    fun getOriginalPurchase(sku: String): Purchase? {
        return purchasesHelper.getPurchase(sku)
    }

    fun getPurchase(sku: String): com.billing.dsl.data.Purchase? {
        return getOriginalPurchase(sku)?.toLibraryInstance()
    }

    suspend fun getOriginalSkuDetails(sku: String): SkuDetails? {
        return skuDetailsHelper.getSkuDetails(sku)
    }

    suspend fun getSkuDetails(sku: String): com.billing.dsl.data.SkuDetails? {
        return getOriginalSkuDetails(sku)?.toLibraryInstance()
    }
}