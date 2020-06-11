package com.codelab.sample

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import kotlinx.android.synthetic.main.activity_main.fab
import kotlinx.android.synthetic.main.activity_main.textView
import kotlinx.android.synthetic.main.activity_main.toolbar

/**
 * @author Shigehiro Soejima
 */
class MainActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PBL Sample"
    }

    private lateinit var billingClient: BillingClient

    // Don't cache skuDetails
    private var skuDetailsList: List<SkuDetails>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { launchPurchase() }

        billingClient =
            BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(p0: BillingResult) {
        when (p0.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "Billing setup successful")
                queryPurchases()
            }
            else -> {
                Log.d(TAG, "Billing setup failed")
            }
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d(TAG, "Billing setup failed")
    }

    override fun onPurchasesUpdated(p0: BillingResult, p1: MutableList<Purchase>?) {
        when (p0.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "onPurchasesUpdated: OK")
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "onPurchasesUpdated: User canceled")
            }
            else -> {
                Toast.makeText(this, "Error: responseCode=${p0.responseCode}", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    private fun queryPurchases() {
        val params = SkuDetailsParams.newBuilder()
            .setSkusList(
                arrayListOf("premium", "gas", "dummy") // dummy won't get any result
            )
            .setType(BillingClient.SkuType.INAPP)
            .build()
        billingClient.querySkuDetailsAsync(params) { billingResult, skuDetailsList ->
            this.skuDetailsList = skuDetailsList

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    skuDetailsList?.apply {
                        if (isNotEmpty()) {
                            forEach {
                                textView.append(it.toString() + "\n\n")
                            }
                        } else {
                            Toast.makeText(this@MainActivity, "No purchases yet", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
                else -> {
                    Log.d(TAG, "Query failed: (response code=${billingResult.responseCode})")
                }
            }
        }
    }

    private fun launchPurchase() {
        skuDetailsList?.let { list ->
            val skuDetail = if (list[0].sku == "Premium") {
                list[0]
            } else {
                list[1]
            }
            val params = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetail)
                .build()
            billingClient.launchBillingFlow(this, params)
        }
    }
}