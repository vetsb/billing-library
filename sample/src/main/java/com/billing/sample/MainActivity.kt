package com.billing.sample

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.billing.dsl.BillingUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext = Job() + Dispatchers.Main

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        updatePurchaseStatuses()

        btnStartPurchaseFlow.run {
            setOnClickListener {
                launch {
                    val result = BillingUtil.startPurchaseFlowAndGetResult(
                        this@MainActivity,
                        "android.test.purchased"
                    )

                    Toast.makeText(this@MainActivity, "$result", Toast.LENGTH_LONG).show()

                    updatePurchaseStatuses()
                }
            }
        }
    }

    private fun updatePurchaseStatuses() {
        tvPurchaseStatuses.run {
            launch(Dispatchers.IO) {
                val sb = StringBuilder()

                BillingUtil.getSkuList().map {
                    sb.append(it)
                    sb.append(" = ")
                    sb.append(BillingUtil.hasPurchase(it))
                    sb.append("\n")
                }

                withContext(Dispatchers.Main) {
                    text = sb.toString()
                }
            }
        }
    }
}
