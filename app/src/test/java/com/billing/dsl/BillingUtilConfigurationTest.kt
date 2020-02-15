package com.billing.dsl

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertTrue
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
}