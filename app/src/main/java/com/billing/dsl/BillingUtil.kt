package com.billing.dsl

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.SkuDetails
import com.billing.dsl.data.ConnectionResult
import com.billing.dsl.data.PurchaseFlowResult
import com.billing.dsl.helper.initialization.InitializationHelper
import com.billing.dsl.helper.initialization.InitializationHelperImpl
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelper
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelperImpl
import com.billing.dsl.helper.purchases.PurchasesHelper
import com.billing.dsl.helper.purchases.PurchasesHelperImpl
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.helper.sku_details.SkuDetailsHelperImpl
import com.billing.dsl.logger.Logger
import kotlinx.coroutines.*

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

        launch {
            initializationHelper.addListener(purchaseFlowHelper)

            val connectionResult = withContext(Dispatchers.Main) {
                initializationHelper.initialize(configuration.context)
            }

            Logger.log("connectionResult $connectionResult")

            if (connectionResult == ConnectionResult.FAILURE) {
                return@launch
            }

            skuDetailsHelper.run {
                billingClient = initializationHelper.billingClient

                fetchSkuDetails(configuration.skuList)
            }

            purchaseFlowHelper.run {
                billingClient = initializationHelper.billingClient
            }

            purchasesHelper.run {
                billingClient = initializationHelper.billingClient
            }
        }

        isInitialized = true
    }

    fun hasPurchase(sku: String): Boolean {
        return purchasesHelper.hasPurchase(sku)
    }

    suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        sku: String
    ): PurchaseFlowResult {
        val skuDetails = withContext(Dispatchers.IO) {
            skuDetailsHelper
                .getSkuDetails()
                .firstOrNull { it.sku == sku }
        } ?: return PurchaseFlowResult.ERROR

        return withContext(Dispatchers.Main) {
            purchaseFlowHelper.startPurchaseFlowAndGetResult(activity, skuDetails)
        }
    }

    suspend fun getSkuDetails(sku: String): SkuDetails? {
        return skuDetailsHelper.getSkuDetails()
            .firstOrNull { it.sku == sku }
    }
}