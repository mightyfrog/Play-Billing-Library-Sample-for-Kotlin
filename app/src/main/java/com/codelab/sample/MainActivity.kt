package com.codelab.sample

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.*

/**
 * @author Shigehiro Soejima
 */
class MainActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener {

    companion object {
        private val TAG = "PBL Sample"
    }

    private var billingClient: BillingClient? = null

    private var textView: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(findViewById(R.id.toolbar))

        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { launchPurchase() }

        textView = findViewById(R.id.textView)

        billingClient = BillingClient.newBuilder(this).setListener(this).build()
        billingClient?.startConnection(this)
    }

    override fun onBillingSetupFinished(resultCode: Int) {
        when (resultCode) {
            BillingClient.BillingResponse.OK -> {
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

    override fun onPurchasesUpdated(responseCode: Int, purchases: List<Purchase>?) {
        when (responseCode) {
            BillingClient.BillingResponse.OK -> {
                Log.d(TAG, "onPurchasesUpdated: OK")
            }
            BillingClient.BillingResponse.USER_CANCELED -> {
                Log.d(TAG, "onPurchasesUpdated: User canceled")
            }
            else -> {
                Log.d(TAG, "onPurchasesUpdated: responseCode=" + responseCode)
            }
        }
    }

    private fun queryPurchases() {
        billingClient?.let {
            val params = SkuDetailsParams.newBuilder()
                    .setSkusList(arrayListOf("premium", "gas", "dummy"))
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            it.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
                when (responseCode) {
                    BillingClient.BillingResponse.OK -> {
                        if (skuDetailsList.isNotEmpty()) {
                            for (sd in skuDetailsList) {
                                textView?.append(sd.toString() + "\n\n")
                            }
                        } else {
                            Toast.makeText(this, "No purchases yet", Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                        Log.d(TAG, "Query failed: (response code=$responseCode)")
                    }
                }
            }
        }
    }

    private fun launchPurchase() {
        billingClient?.let {
            val params = BillingFlowParams.newBuilder()
                    .setSku("gas")
                    .setType(BillingClient.SkuType.INAPP)
                    .build()
            it.launchBillingFlow(this, params)
        }
    }
}