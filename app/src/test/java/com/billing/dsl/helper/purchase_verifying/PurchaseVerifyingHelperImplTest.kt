package com.billing.dsl.helper.purchase_verifying

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.billing.dsl.helper.sku_details.SkuDetailsHelper
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PurchaseVerifyingHelperImplTest {

    @Mock
    private lateinit var skuDetailsHelper: SkuDetailsHelper

    @Mock
    private lateinit var billingClient: BillingClient

    private lateinit var purchaseVerifyingHelper: PurchaseVerifyingHelperImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        purchaseVerifyingHelper = PurchaseVerifyingHelperImpl(skuDetailsHelper).apply {
            billingClient = this@PurchaseVerifyingHelperImplTest.billingClient
        }
    }

    @Test
    fun whenVerifyingPurchase_Acknowledged_ShouldCallAnything() = runBlocking {
        purchaseVerifyingHelper.verify(acknowledgedPurchase)

        verifyZeroInteractions(skuDetailsHelper, billingClient)

        Unit
    }

    @Test
    fun whenVerifyingPurchase_NotAcknowledged_ShouldCallGetSkuDetails() = runBlocking {
        purchaseVerifyingHelper.verify(notAcknowledgedPurchase)

        verify(skuDetailsHelper).getSkuDetails(notAcknowledgedPurchase.sku)

        Unit
    }

    companion object {
        private val acknowledgedPurchase = spy(
            Purchase(
                JSONObject().apply {
                    put("productId", "test_sku")
                    put("acknowledged", true)
                }.toString(0),
                "TEST_SIGNATURE"
            )
        )

        private val notAcknowledgedPurchase = spy(
            Purchase(
                JSONObject().apply {
                    put("productId", "test_sku")
                    put("acknowledged", false)
                    put("purchaseToken", "TOKEN")
                }.toString(0),
                "TEST_SIGNATURE"
            )
        )
    }
}