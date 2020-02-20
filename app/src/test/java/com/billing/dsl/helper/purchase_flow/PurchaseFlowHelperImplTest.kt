package com.billing.dsl.helper.purchase_flow

import android.app.Activity
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.billing.dsl.constant.ResponseCode
import com.billing.dsl.helper.purchase_verifying.PurchaseVerifyingHelper
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PurchaseFlowHelperImplTest {

    @Mock
    private lateinit var billingClient: BillingClient

    @Mock
    private lateinit var purchaseVerifyingHelper: PurchaseVerifyingHelper

    @Mock
    private lateinit var skuDetailsHelper: SkuDetailsHelper

    private lateinit var purchaseFlowHelper: PurchaseFlowHelperImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        purchaseFlowHelper = PurchaseFlowHelperImpl(
            purchaseVerifyingHelper,
            skuDetailsHelper
        ).apply {
            billingClient = this@PurchaseFlowHelperImplTest.billingClient
        }
    }

    @Test
    fun whenStartingFlow_And_ThereIsNotSkuDetails_ShouldReturnError() = runBlocking {
        `when`(skuDetailsHelper.getSkuDetails(ArgumentMatchers.anyString())).thenReturn(null)

        val result = purchaseFlowHelper.startPurchaseFlowAndGetResult(
            mock(Activity::class.java),
            ""
        )

        assertEquals(result, ResponseCode.ERROR)

        Unit
    }

//    @Test
//    fun whenStartingFlow_And_ThereIsSkuDetails_ShouldCallLaunchBillingFlow() = runBlocking {
//        val skuDetails = mock(SkuDetails::class.java)
//        val activity = mock(Activity::class.java)
//
//        `when`(skuDetailsHelper.getSkuDetails(ArgumentMatchers.anyString())).thenReturn(skuDetails)
//
//        launch {
//            delay(2000)
//
//            val billingResult = BillingResult.newBuilder()
//                .setResponseCode(BillingClient.BillingResponseCode.USER_CANCELED)
//                .build()
//
//            purchaseFlowHelper.onPurchasesUpdated(billingResult, mutableListOf())
//        }
//
//        purchaseFlowHelper.startPurchaseFlowAndGetResult(activity, "")
//
////        doCallRealMethod().`when`(billingClient).launchBillingFlow(activity, ArgumentMatchers.any(BillingFlowParams::class.java))
//
//        verify(billingClient).launchBillingFlow(activity, ArgumentMatchers.any(BillingFlowParams::class.java))
//
//        Unit
//    }
}