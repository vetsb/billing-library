package com.billing.dsl.helper.initialization

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class InitializationHelperImplTest {

    private lateinit var initializationHelper: InitializationHelper

    @Before
    fun setUp() {
        initializationHelper = InitializationHelperImpl()
    }

    @Test
    fun whenInitialized_shouldCreateBillingClient() = runBlocking {
        initializationHelper.initialize(getApplicationContext())

        assertNotNull(initializationHelper.billingClient)

        Unit

//        verify(initializationHelper.billingClient?.startConnection(any()), times(1))
    }
}