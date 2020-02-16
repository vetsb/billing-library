package com.billing.dsl.helper.purchases

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class PurchasesHelperImplTest {

    @Mock
    private lateinit var billingClient: BillingClient

    private lateinit var purchasesHelper: PurchasesHelperImpl

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        purchasesHelper = PurchasesHelperImpl().apply {
            billingClient = this@PurchasesHelperImplTest.billingClient
        }
    }

    @Test
    fun hasPurchase_IfThereIsPurchaseWithSku_ShouldReturnTrue() {
        makeQueryPurchasesReturnNotEmptyResult()

        val result = purchasesHelper.hasPurchase("test_sku")

        assertTrue(result)
    }

    @Test
    fun hasPurchase_IfThereIsNotPurchaseWithSku_ShouldReturnFalse() {
        makeQueryPurchasesReturnEmptyResult()

        val result = purchasesHelper.hasPurchase("test_sku")

        assertFalse(result)
    }

    @Test
    fun getPurchases_ShouldQueryInAppPurchases() {
        makeQueryPurchasesReturnNotEmptyResult()

        purchasesHelper.getPurchases()

        verify(billingClient).queryPurchases(BillingClient.SkuType.INAPP)
    }

    @Test
    fun getPurchases_ShouldQuerySubsPurchases() {
        makeQueryPurchasesReturnEmptyResult()

        purchasesHelper.getPurchases()

        verify(billingClient).queryPurchases(BillingClient.SkuType.SUBS)
    }

    @Test
    fun getPurchase_IfThereIsPurchaseWithSku_ShouldReturnIt() {
        makeQueryPurchasesReturnNotEmptyResult()

        val purchase = purchasesHelper.getPurchase("test_sku")

        assertEquals(purchase?.sku, "test_sku")
    }

    @Test
    fun getPurchase_IfThereIsNotPurchaseWithSku_ShouldReturnNull() {
        makeQueryPurchasesReturnEmptyResult()

        val purchase = purchasesHelper.getPurchase("test_sku")

        assertNull(purchase)
    }

    private fun makeQueryPurchasesReturnNotEmptyResult() {
        `when`(billingClient.queryPurchases(ArgumentMatchers.anyString())).thenReturn(
            Purchase.PurchasesResult(
                BillingResult(),
                listOf(
                    Purchase(
                        "{\"productId\": \"test_sku\"}",
                        "TEST_SIGNATURE"
                    )
                )
            )
        )
    }

    private fun makeQueryPurchasesReturnEmptyResult() {
        `when`(billingClient.queryPurchases(ArgumentMatchers.anyString())).thenReturn(
            Purchase.PurchasesResult(
                BillingResult(),
                listOf()
            )
        )
    }
}