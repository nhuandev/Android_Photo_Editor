package com.example.appphotointern.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(private val context: Context) {
    companion object {
        const val PREMIUM_WEEKLY_ID = "premium_weekly"
        const val PURCHASE_YEARLY_ID = "premium_yearly"
        const val LIFETIME_ID = "lifetime_premium"
        private const val TAG = "BillingManager"
        private const val PREFS_NAME = "billing_prefs"
        private const val KEY_IS_PREMIUM = "key_is_premium"
    }

    private val _productDetails = MutableLiveData<List<ProductDetails>>()
    val productDetails: LiveData<List<ProductDetails>> get() = _productDetails

    private val _purchaseStatus = MutableLiveData<Boolean?>()
    val purchaseStatus: LiveData<Boolean?> get() = _purchaseStatus

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isPremium = MutableLiveData(prefs.getBoolean(KEY_IS_PREMIUM, false))
    val isPremium: LiveData<Boolean> get() = _isPremium

    fun isPremiumSync(): Boolean = prefs.getBoolean(KEY_IS_PREMIUM, false)

    private fun updatePremiumState(isPremiumNow: Boolean) {
        _isPremium.postValue(isPremiumNow)
        prefs.edit().putBoolean(KEY_IS_PREMIUM, isPremiumNow).apply()
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .enablePendingPurchases()
        .setListener { billingResult, purchases ->
            handlePurchaseUpdate(billingResult, purchases)
        }
        .build()

    fun startConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "Billing service disconnected")
            }

            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d(TAG, "Billing setup finished successfully")
                    queryAvailableProducts()
                    queryCurrentPurchases()
                } else {
                    Log.e(TAG, "Billing setup failed: ${result.debugMessage}")
                }
            }
        })
    }

    private fun queryAvailableProducts() {
        val loadedDetails = mutableListOf<ProductDetails>()

        // Query subscription products
        val subsProducts = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_WEEKLY_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PURCHASE_YEARLY_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProducts)
            .build()

        billingClient.queryProductDetailsAsync(subsParams) { subsResult, subsList ->
            if (subsResult.responseCode == BillingClient.BillingResponseCode.OK) {
                loadedDetails.addAll(subsList)
            } else {
                Log.e(TAG, "Failed to query SUBS: ${subsResult.debugMessage}")
            }

            // Query in-app products
            val inAppProducts = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(LIFETIME_ID)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )
            val inAppParams = QueryProductDetailsParams.newBuilder()
                .setProductList(inAppProducts)
                .build()

            billingClient.queryProductDetailsAsync(inAppParams) { inAppResult, inAppList ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    loadedDetails.addAll(inAppList)
                } else {
                    Log.e(TAG, "Failed to query INAPP: ${inAppResult.debugMessage}")
                }
                _productDetails.postValue(loadedDetails)
            }
        }
    }

    fun queryCurrentPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "BillingClient is not ready, cannot query purchases")
            return
        }

        // Track trạng thái premium
        var hasPremium = false

        // Query SUBS
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                purchases.forEach { purchase ->
                    if (checkPurchaseState(purchase)) {
                        hasPremium = true
                    }
                }
            } else {
                Log.e(TAG, "Failed to query SUBS purchases: ${result.debugMessage}")
            }

            // Query INAPP
            billingClient.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ) { inAppResult, inAppPurchases ->
                if (inAppResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    inAppPurchases.forEach { purchase ->
                        if (checkPurchaseState(purchase)) {
                            hasPremium = true
                        }
                    }
                } else {
                    Log.e(TAG, "Failed to query INAPP purchases: ${inAppResult.debugMessage}")
                }

                // ✅ Sau khi query cả SUBS và INAPP xong, update final state
                updatePremiumState(hasPremium)
            }
        }
    }

    private fun checkPurchaseState(purchase: Purchase): Boolean {
        return when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                purchase.products.any { productId ->
                    productId == PREMIUM_WEEKLY_ID ||
                            productId == PURCHASE_YEARLY_ID ||
                            productId == LIFETIME_ID
                }
            }
            else -> false
        }
    }

    private fun handlePurchaseUpdate(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            purchases.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.purchaseToken)
                            .build()
                        billingClient.acknowledgePurchase(ackParams) { ackResult ->
                            if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                _purchaseStatus.postValue(true)
                                updatePremiumState(true)
                            } else {
                                _purchaseStatus.postValue(false)
                                Log.e(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                            }
                        }
                    } else {
                        _purchaseStatus.postValue(true)
                        updatePremiumState(true)
                    }
                }
            }
        } else if (result.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _purchaseStatus.postValue(false)
        } else {
            _purchaseStatus.postValue(false)
            Log.e(TAG, "Purchase failed: ${result.debugMessage}")
            // In case of failure, keep existing persisted state; do not flip unless we know otherwise
        }
    }

    fun launchPurchaseFlow(activity: Activity, productDetails: ProductDetails) {
        val paramsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == BillingClient.ProductType.SUBS) {
            productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken?.let { token ->
                paramsBuilder.setOfferToken(token)
            }
        }

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(paramsBuilder.build()))
            .build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun findProductDetailsById(productId: String): ProductDetails? {
        return _productDetails.value?.firstOrNull { it.productId == productId }
    }
}
