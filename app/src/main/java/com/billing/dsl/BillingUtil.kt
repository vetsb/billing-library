package com.billing.dsl

import android.app.Activity
import android.content.Context
import com.billing.dsl.data.Purchase
import com.billing.dsl.data.SkuDetails
import com.billing.dsl.helper.initialization.InitializationHelper
import com.billing.dsl.helper.initialization.InitializationHelperImpl
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelper
import com.billing.dsl.helper.purchase_flow.PurchaseFlowHelperImpl
import com.billing.dsl.helper.purchase_verifying.PurchaseVerifyingHelper
import com.billing.dsl.helper.purchase_verifying.PurchaseVerifyingHelperImpl
import com.billing.dsl.helper.purchases.PurchasesHelper
import com.billing.dsl.helper.purchases.PurchasesHelperImpl
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import com.billing.dsl.helper.sku_details.SkuDetailsHelperImpl
import com.billing.dsl.logger.Logger
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

    private val purchaseVerifyingHelper: PurchaseVerifyingHelper by lazy {
        PurchaseVerifyingHelperImpl(skuDetailsHelper)
    }

    private val purchaseFlowHelper: PurchaseFlowHelper by lazy {
        PurchaseFlowHelperImpl(purchaseVerifyingHelper, skuDetailsHelper)
    }

    private val purchasesHelper: PurchasesHelper by lazy {
        PurchasesHelperImpl()
    }

    private var isInitialized = false

    class Configuration(
        val context: Context
    ) {

        internal var skuList = arrayListOf<String>()

        internal var isLoggingEnabled: Boolean? = null

        internal var isAcknowledgeEnabled: Boolean? = null

        fun setLoggingEnabled(value: Boolean): Configuration {
            isLoggingEnabled = value

            return this
        }

        fun setAcknowledgeEnabled(value: Boolean): Configuration {
            isAcknowledgeEnabled = value

            return this
        }

        fun addSku(sku: String): Configuration {
            if (sku.isNotEmpty()) {
                skuList.add(sku)
            }

            return this
        }

        fun addSkuList(list: List<String>): Configuration {
            list.forEach {
                addSku(it)
            }

            return this
        }
    }

    @Throws(IllegalStateException::class)
    fun initialize(configuration: Configuration) {
        check(!isInitialized) { "BillingUtil has been initialized already" }

        configuration.run {
            isLoggingEnabled?.let {
                Logger.isEnabled = it
            }

            isAcknowledgeEnabled?.let {
                purchaseFlowHelper.isAcknowledgeEnabled = it
            }
        }

        launch(Dispatchers.Main) {
            initializationHelper.addListener(purchaseFlowHelper)
            initializationHelper.initialize(configuration.context)

            listOf(
                skuDetailsHelper,
                purchaseFlowHelper,
                purchasesHelper,
                purchaseVerifyingHelper
            ).forEach {
                it.billingClient = initializationHelper.billingClient
            }

            skuDetailsHelper.run {
                fetchSkuDetails(configuration.skuList)
            }
        }

        isInitialized = true
    }

    fun getSkuList() = skuDetailsHelper.getSkuList()

    suspend fun startPurchaseFlowAndGetResult(
        activity: Activity,
        sku: String
    ) = purchaseFlowHelper.startPurchaseFlowAndGetResult(activity, sku)

    fun hasPurchase(sku: String) = purchasesHelper.hasPurchase(sku)

    fun getOriginalPurchases() = purchasesHelper.getPurchases()

    fun getPurchases() = getOriginalPurchases().map { Purchase.fromGooglePurchase(it) }

    suspend fun getOriginalSkuDetails(sku: String) = skuDetailsHelper.getSkuDetails(sku)

    suspend fun getSkuDetails(sku: String) =
        SkuDetails.fromGoogleSkuDetails(getOriginalSkuDetails(sku))
}