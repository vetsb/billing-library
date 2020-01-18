package com.billing.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billing.dsl.BillingUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStartPurchaseFlow.run {
            setOnClickListener {
                launch {
                    val result = BillingUtil.startPurchaseFlowAndGetResult(
                        this@MainActivity,
                        "android.test.purchased"
                    )

                    Toast.makeText(this@MainActivity, "$result", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
