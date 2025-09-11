package com.example.appphotointern.ui.purchase

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.Purchase.PurchaseState
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.appphotointern.R
import com.example.appphotointern.extention.toast
import com.example.appphotointern.utils.PurchasePrefs
import com.google.common.collect.ImmutableList

class BillingManager(private val context: Context) {
    companion object {
        const val IN_APP_LIFE_TIME = "lifetime_premium"
        const val SUBS_PREMIUM_WEEKLY = "premium_weekly"
        const val SUBS_PREMIUM_YEARLY = "premium_yearly"
        const val TAG = "Purchase-Manager"
    }

    private var _productDetails = MutableLiveData<List<ProductDetails>>()
    val productDetails: MutableLiveData<List<ProductDetails>> get() = _productDetails

    private var _purchaseStatus = MutableLiveData<Boolean?>()
    val purchaseStatus: MutableLiveData<Boolean?> get() = _purchaseStatus

    private val purchasesUpdatedListener =
        PurchasesUpdatedListener { blResult, purchases ->
            when (blResult.responseCode) {
                BillingResponseCode.OK -> {
                    purchases?.let {
                        handlePurchase(blResult, purchases)
                    }
                }

                BillingResponseCode.USER_CANCELED -> {
                    context.toast(R.string.lb_toast_purchase_cancel)
                    _purchaseStatus.postValue(null)
                }

                BillingResponseCode.NETWORK_ERROR -> {
                    context.toast(R.string.lb_toast_network_error)
                    _purchaseStatus.postValue(false)
                }

                else -> {
                    context.toast(R.string.lb_toast_purchase_error)
                    _purchaseStatus.postValue(false)
                }
            }
        }

    private val billingClient: BillingClient =
        BillingClient.newBuilder(context)
            .setListener(purchasesUpdatedListener)
            .enablePendingPurchases()
            .build()

    fun startBillingConnect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingResponseCode.OK) {
                    productsAvailable()
                    productPurchased()
                } else {
                    context.toast(R.string.lb_toast_purchase_not_ready)
                }
            }

            override fun onBillingServiceDisconnected() {
                context.toast(R.string.lb_toast_purchase_disconnect)
            }
        })
    }

    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
        if (!billingClient.isReady) {
            context.toast(R.string.lb_toast_purchase_not_ready)
            return
        }

        val params = BillingFlowParams
            .ProductDetailsParams
            .newBuilder()
            .setProductDetails(productDetails)

        if (productDetails.productType == ProductType.SUBS) {
            val tokenSubs = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            tokenSubs?.let {
                params.setOfferToken(tokenSubs)
            } ?: run {
                context.toast(R.string.lb_toast_purchase_error)
                return
            }
        }
        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(params.build())).build()
        billingClient.launchBillingFlow(activity, flowParams)
    }

    private fun productPurchased() {
        if (!billingClient.isReady) {
            context.toast(R.string.lb_toast_purchase_not_ready)
            return
        }
        var inAppChecked = false
        var subsChecked = false
        var hasPremium = false
        fun updatePremiumStatus() {
            if (subsChecked && inAppChecked) {
                PurchasePrefs(context).hasPremium = hasPremium
                Log.d(TAG, "Final premium status: $hasPremium")
            }
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.SUBS)
                .build()
        ) { blResult, purchases ->
            if (blResult.responseCode == BillingResponseCode.OK && purchases.isNotEmpty()) {
                val hasValidSub = purchases.any {
                    it.purchaseState == PurchaseState.PURCHASED
                }
                purchases.forEach {
                    handlePurchase(blResult, listOf(it))
                }
                Log.d(TAG, "Has valid subscription: $hasValidSub")
                if (hasValidSub) hasPremium = true
            }
            subsChecked = true
            updatePremiumStatus()
        }

        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(ProductType.INAPP)
                .build()
        ) { blResult, purchases ->
            if (blResult.responseCode == BillingResponseCode.OK && purchases.isNotEmpty()) {
                val hasValidInApp = purchases.any {
                    it.purchaseState == PurchaseState.PURCHASED
                }
                purchases.forEach {
                    handlePurchase(blResult, listOf(it))
                }
                Log.d(TAG, "Has valid in-app purchase: $hasValidInApp")
                if (hasValidInApp) hasPremium = true
            }
            inAppChecked = true
            updatePremiumStatus()
        }
    }

    fun findProductDetailsById(productId: String): ProductDetails? {
        return _productDetails.value?.firstOrNull {
            it.productId == productId
        }
    }

    private fun handlePurchase(blResult: BillingResult, purchases: List<Purchase>?) {
        if (blResult.responseCode != BillingResponseCode.OK) {
            context.toast(R.string.lb_toast_purchase_error)
            _purchaseStatus.postValue(false)
            return
        }

        if (purchases.isNullOrEmpty()) {
            context.toast(R.string.lb_toast_purchase_error)
            _purchaseStatus.postValue(false)
            return
        }

        var purchaseProcessed = false
        purchases.forEach { purchaseFirst ->
            Log.d(TAG, "product=${purchaseFirst}")
            when (purchaseFirst.purchaseState) {
                PurchaseState.PURCHASED -> {
                    if (!purchaseFirst.isAcknowledged) {
                        val ackParams = AcknowledgePurchaseParams
                            .newBuilder()
                            .setPurchaseToken(purchaseFirst.purchaseToken)
                            .build()

                        billingClient.acknowledgePurchase(ackParams) { ackResult ->
                            if (ackResult.responseCode == BillingResponseCode.OK) {
                                Log.d(TAG, "Acknowledge success")
                                PurchasePrefs(context).hasPremium = true
                                _purchaseStatus.postValue(true)
                                context.toast(R.string.lb_toast_purchase_success)
                            } else {
                                _purchaseStatus.postValue(false)
                                Log.e(TAG, "Acknowledge failed: ${ackResult.debugMessage}")
                            }
                        }
                    } else {
                        Log.d(TAG, "Purchase already acknowledged")
                        context.toast(R.string.lb_toast_purchase_success)
                    }
                }

                PurchaseState.PENDING -> {
                    Log.d(TAG, "Purchase pending")
                    _purchaseStatus.postValue(null)
                    purchaseProcessed = true
                }
            }
        }
    }

    private fun productsAvailable() {
        val loadProducts = mutableListOf<ProductDetails>()
        var inAppLoaded = false
        var subsLoaded = false

        fun updateProductDetails() {
            if (inAppLoaded && subsLoaded) {
                _productDetails.postValue(loadProducts.toList())
                Log.d(TAG, "All products loaded: ${loadProducts.size}")
            }
        }

        val inAppProduct = ImmutableList.of(
            QueryProductDetailsParams.Product
                .newBuilder().setProductId(IN_APP_LIFE_TIME)
                .setProductType(ProductType.INAPP)
                .build()
        )

        val inAppParams = QueryProductDetailsParams.newBuilder()
            .setProductList(inAppProduct)
            .build()

        billingClient.queryProductDetailsAsync(inAppParams) { blResult, productDetails ->
            if (blResult.responseCode == BillingResponseCode.OK && productDetails.isNotEmpty()) {
                synchronized(loadProducts) {
                    loadProducts.addAll(productDetails)
                }
                Log.d(TAG, "In-app products loaded: ${productDetails.size}")
            } else {
                Log.d(TAG, "Failed to query in-app product details: ${blResult.debugMessage}")
            }
            inAppLoaded = true
            updateProductDetails()
        }

        val subsProduct = ImmutableList.of(
            QueryProductDetailsParams.Product
                .newBuilder().setProductId(SUBS_PREMIUM_WEEKLY)
                .setProductType(ProductType.SUBS)
                .build(),

            QueryProductDetailsParams.Product
                .newBuilder().setProductId(SUBS_PREMIUM_YEARLY)
                .setProductType(ProductType.SUBS)
                .build()
        )

        val subsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(subsProduct)
            .build()

        billingClient.queryProductDetailsAsync(subsParams) { blResult, productDetails ->
            if (blResult.responseCode == BillingResponseCode.OK && productDetails.isNotEmpty()) {
                synchronized(loadProducts) {
                    loadProducts.addAll(productDetails)
                }
                Log.d(TAG, "Subscription products loaded: ${productDetails.size}")
            } else {
                Log.d(TAG, "Failed to query subscription product details: ${blResult.debugMessage}")
            }
            subsLoaded = true
            updateProductDetails()
        }
    }
}