package com.example.appphotointern.ui.purchase

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding
import kotlinx.coroutines.*

class PurchaseActivity : BaseActivity(), PurchasesUpdatedListener {

    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }
    private lateinit var billingClient: BillingClient
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    companion object {
        private const val TAG = "BillingDebug"

        // Product IDs theo mentor
        const val PREMIUM_WEEKLY_ID = "premium_weekly"
        const val PREMIUM_YEARLY_ID = "premium_yearly"
        const val PREMIUM_LIFETIME_ID = "lifetime_premium"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.btnContinue.text = "Testing Billing..."
        binding.btnContinue.isEnabled = false

        // Immediate debug info
        logEnvironmentInfo()

        // Test billing step by step
        testBillingFlow()
    }

    private fun logEnvironmentInfo() {
        Log.d(TAG, "=== ENVIRONMENT INFO ===")
        Log.d(TAG, "Package: ${packageName}")
        Log.d(TAG, "Expected: com.azmobile.phonemirror")
        Log.d(TAG, "Match: ${packageName == "com.azmobile.phonemirror"}")

        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            Log.d(TAG, "App version: ${pInfo.versionName} (${pInfo.versionCode})")
        } catch (e: Exception) {
            Log.e(TAG, "Can't get package info", e)
        }

        // Check Google Play Store
        try {
            packageManager.getPackageInfo("com.android.vending", 0)
            Log.d(TAG, "✅ Google Play Store installed")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Google Play Store NOT installed", e)
        }

        // Check Google Play Services
        try {
            val gmsInfo = packageManager.getPackageInfo("com.google.android.gms", 0)
            Log.d(TAG, "✅ Play Services version: ${gmsInfo.versionName}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Google Play Services not available", e)
        }
    }

    private fun testBillingFlow() {
        Log.d(TAG, "=== STEP 1: Initialize BillingClient ===")

        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        Log.d(TAG, "BillingClient created, starting connection...")
        binding.btnContinue.text = "Connecting..."

        // Set timeout for connection
        scope.launch {
            delay(10000) // 10 seconds timeout
            if (!billingClient.isReady) {
                Log.e(TAG, "❌ CONNECTION TIMEOUT after 10 seconds")
                binding.btnContinue.text = "Connection timeout"
                showError("Connection timeout. Check your internet and Play Store.")
            }
        }

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d(TAG, "=== BILLING SETUP FINISHED ===")
                Log.d(TAG, "Response Code: ${billingResult.responseCode}")
                Log.d(TAG, "Debug Message: ${billingResult.debugMessage}")

                when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.d(TAG, "✅ Connection successful!")
                        binding.btnContinue.text = "Connected! Querying..."
                        testQueryProducts()
                    }

                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                        Log.e(TAG, "❌ BILLING_UNAVAILABLE - App not on Play Store or wrong signing")
                        showError("App not published or wrong keystore")
                    }

                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                        Log.e(TAG, "❌ SERVICE_UNAVAILABLE - Network issue")
                        showError("Network error or Play Store unavailable")
                    }

                    else -> {
                        Log.e(TAG, "❌ Setup failed: ${billingResult.responseCode}")
                        showError("Setup failed: ${billingResult.debugMessage}")
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "⚠️ Billing disconnected")
                binding.btnContinue.text = "Disconnected"
            }
        })
    }

    private fun testQueryProducts() {
        Log.d(TAG, "=== STEP 2: Query Products ===")

        if (!billingClient.isReady) {
            Log.e(TAG, "❌ BillingClient not ready")
            return
        }

        // Test with just ONE product first
        Log.d(TAG, "Testing with single product: $PREMIUM_WEEKLY_ID")

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_YEARLY_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        Log.d(TAG, "Sending query request...")

        // Set timeout for query
        scope.launch {
            delay(15000) // 15 seconds timeout
            Log.e(TAG, "❌ QUERY TIMEOUT after 15 seconds")
            binding.btnContinue.text = "Query timeout"
            showError("Query timeout. Product might not exist on Play Console.")
        }

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            Log.d(TAG, "=== QUERY RESULT ===")
            Log.d(TAG, "Response Code: ${billingResult.responseCode}")
            Log.d(TAG, "Debug Message: ${billingResult.debugMessage}")
            Log.d(TAG, "Products Count: ${productDetailsList.size}")

            when (billingResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (productDetailsList.isNotEmpty()) {
                        Log.d(TAG, "✅ SUCCESS! Product found:")
                        productDetailsList.forEach { product ->
                            Log.d(TAG, "  - ID: ${product.productId}")
                            Log.d(TAG, "  - Title: ${product.title}")
                            Log.d(TAG, "  - Description: ${product.description}")
                            Log.d(TAG, "  - Price: ${product.oneTimePurchaseOfferDetails?.formattedPrice}")
                        }

                        runOnUiThread {
                            binding.btnContinue.text = "Success! Found ${productDetailsList.size} products"
                            binding.btnContinue.isEnabled = true
                            Toast.makeText(this@PurchaseActivity, "✅ Products found!", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e(TAG, "❌ SUCCESS but NO PRODUCTS - Product ID not found on Play Console")
                        showError("Product '$PREMIUM_WEEKLY_ID' not found on Play Console")
                    }
                }

                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                    Log.e(TAG, "❌ SERVICE_UNAVAILABLE during query")
                    showError("Play Store service unavailable")
                }

                else -> {
                    Log.e(TAG, "❌ Query failed: ${billingResult.debugMessage}")
                    showError("Query failed: ${billingResult.debugMessage}")
                }
            }
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            binding.btnContinue.text = "Error"
            binding.btnContinue.isEnabled = true
            Toast.makeText(this, "❌ $message", Toast.LENGTH_LONG).show()

            // Add retry button
            binding.btnContinue.setOnClickListener {
                recreate() // Restart activity
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        // Not needed for this test
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        if (::billingClient.isInitialized) {
            billingClient.endConnection()
        }
    }
}