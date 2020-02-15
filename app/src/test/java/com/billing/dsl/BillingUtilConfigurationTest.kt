package com.billing.dsl

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class BillingUtilConfigurationTest {

    private lateinit var configuration: BillingUtil.Configuration

    @Before
    fun setUp() {
        configuration = BillingUtil.Configuration(getApplicationContext())
    }

    @Test
    fun whenNotSettingLoggingEnabled_LoggingEnabledMustBeNull() {
        assertTrue(configuration.isLoggingEnabled == null)
    }

    @Test
    fun whenSettingLoggingEnabled_False_LoggingEnabledMustBeTrue() {
        configuration.setLoggingEnabled(false)

        assertTrue(configuration.isLoggingEnabled == false)
    }

    @Test
    fun whenSettingLoggingEnabled_True_LoggingEnabledMustBeTrue() {
        configuration.setLoggingEnabled(true)

        assertTrue(configuration.isLoggingEnabled == true)
    }


    @Test
    fun whenNotSettingAcknowledgeEnabled_AcknowledgeEnabledMustBeNull() {
        assertTrue(configuration.isAcknowledgeEnabled == null)
    }

    @Test
    fun whenSettingAcknowledgeEnabled_False_AcknowledgeEnabledMustBeTrue() {
        configuration.setAcknowledgeEnabled(false)

        assertTrue(configuration.isAcknowledgeEnabled == false)
    }

    @Test
    fun whenSettingAcknowledgeEnabled_True_AcknowledgeEnabledMustBeTrue() {
        configuration.setAcknowledgeEnabled(true)

        assertTrue(configuration.isAcknowledgeEnabled == true)
    }


    @Test
    fun whenAddingSku_NotEmpty_InternalListMustContainIt() {
        configuration.addSku("NEW SKU")

        assertThat(configuration.skuList).containsExactly("NEW SKU")
    }

    @Test
    fun whenAddingSku_Empty_InternalListMustContainIt() {
        configuration.addSku("")

        assertThat(configuration.skuList).doesNotContain("")
    }

    @Test
    fun whenAddingSkuList_NotEmptyStrings_InternalListMustContainThem() {
        configuration.addSkuList(listOf("NEW SKU 1", "NEW SKU 2"))

        assertThat(configuration.skuList).containsAll(listOf("NEW SKU 1", "NEW SKU 2"))
    }

    @Test
    fun whenAddingSkuList_NotEmptyString_And_EmptyString_InternalListMustContainOnlyFirst() {
        configuration.addSkuList(listOf("NEW SKU 1", ""))

        assertThat(configuration.skuList)
            .contains("NEW SKU 1")
            .doesNotContain("")
    }

    @Test
    fun whenAddingSkuList_EmptyStrings_InternalListMustNotContainThem() {
        configuration.addSkuList(listOf("", ""))

        assertThat(configuration.skuList)
            .doesNotContainAnyElementsOf(listOf("", ""))
    }
}