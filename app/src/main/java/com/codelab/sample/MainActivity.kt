package com.codelab.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Shigehiro Soejima
 */
class MainActivity : AppCompatActivity(), BillingClientStateListener, PurchasesUpdatedListener {

    companion object {
        private const val TAG = "PBL Sample"
    }

    private lateinit var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { launchPurchase() }

        billingClient = BillingClient.newBuilder(this).setListener(this).build()
        billingClient.startConnection(this)
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
                Log.d(TAG, "onPurchasesUpdated: responseCode=$responseCode")
            }
        }
    }

    private fun queryPurchases() {
        val params = SkuDetailsParams.newBuilder()
                .setSkusList(arrayListOf("premium", "gas", "dummy"))
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.querySkuDetailsAsync(params) { responseCode, skuDetailsList ->
            when (responseCode) {
                BillingClient.BillingResponse.OK -> {
                    if (skuDetailsList.isNotEmpty()) {
                        skuDetailsList.forEach {
                            textView.append(it.toString() + "\n\n")
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

    private fun launchPurchase() {
        val params = BillingFlowParams.newBuilder()
                .setSku("gas")
                .setType(BillingClient.SkuType.INAPP)
                .build()
        billingClient.launchBillingFlow(this, params)
    }
}