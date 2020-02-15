package com.billing.dsl.helper.initialization

import android.os.Build
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.billingclient.api.PurchasesUpdatedListener
import junit.framework.Assert.assertNotNull
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])
class InitializationHelperImplTest {

    private lateinit var initializationHelper: InitializationHelperImpl

    @Before
    fun setUp() {
        initializationHelper = InitializationHelperImpl()

        runBlocking {
            initializationHelper.initialize(getApplicationContext())
        }
    }

    @Test
    fun whenInitialized_shouldCreateBillingClient() {
        assertNotNull(initializationHelper.billingClient)
    }

    @Test
    fun whenInitialized_shouldCallStartConnection() {
        val spy = spy(initializationHelper.billingClient!!)

        doCallRealMethod().`when`(spy).startConnection(any())
    }

    @Test
    fun whenAddListener_shouldAddListenerToTheInternalList() {
        val listener = mock(PurchasesUpdatedListener::class.java)

        initializationHelper.addListener(listener)

        assertThat(initializationHelper.listeners).containsExactly(listener)
    }
}