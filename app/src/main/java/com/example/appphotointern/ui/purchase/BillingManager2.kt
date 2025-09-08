//package com.example.appphotointern.ui.purchase
//
//import android.app.Activity
//import android.content.Context
//import android.util.Log
//import androidx.lifecycle.MutableLiveData
//import com.android.billingclient.api.AcknowledgePurchaseParams
//import com.android.billingclient.api.BillingClient
//import com.android.billingclient.api.BillingClient.BillingResponseCode
//import com.android.billingclient.api.BillingClient.ProductType
//import com.android.billingclient.api.BillingClientStateListener
//import com.android.billingclient.api.BillingFlowParams
//import com.android.billingclient.api.BillingResult
//import com.android.billingclient.api.ProductDetails
//import com.android.billingclient.api.Purchase
//import com.android.billingclient.api.Purchase.PurchaseState
//import com.android.billingclient.api.PurchasesUpdatedListener
//import com.android.billingclient.api.QueryProductDetailsParams
//import com.android.billingclient.api.QueryPurchasesParams
//import com.example.appphotointern.R
//import com.example.appphotointern.extention.toast
//import com.example.appphotointern.utils.PurchasePrefs
//import com.google.common.collect.ImmutableList
//
//class BillingManager2(private val context: Context) {
//    companion object {
//        const val IN_APP_LIFE_TIME = "lifetime_premium"
//        const val SUBS_PREMIUM_WEEKLY = "premium_weekly"
//        const val SUBS_PREMIUM_YEARLY = "premium_yearly"
//        const val TAG = "Purchase-Manager"
//    }
//
//    private var _productDetails = MutableLiveData<List<ProductDetails>>()
//    val productDetails: MutableLiveData<List<ProductDetails>> get() = _productDetails
//
//    private var _purchaseStatus = MutableLiveData<Boolean?>()
//    val purchaseStatus: MutableLiveData<Boolean?> get() = _purchaseStatus
//
//    private val purchasesUpdatedListener =
//        PurchasesUpdatedListener { blResult, purchases ->
//            when (blResult.responseCode) {
//                BillingResponseCode.OK -> {
//                    if (purchases != null) {
//                        handlePurchase(blResult, purchases)
//                    }
//                }
//
//                BillingResponseCode.USER_CANCELED -> {
//                    context.toast(R.string.lb_toast_purchase_cancel)
//                }
//
//                BillingResponseCode.NETWORK_ERROR -> {
//                    context.toast(R.string.lb_toast_network_error)
//                }
//            }
//        }
//
//    private val billingClient: BillingClient =
//        BillingClient.newBuilder(context)
//            .setListener(purchasesUpdatedListener)
//            .enablePendingPurchases()
//            .build()
//
//    fun startBillingConnect() {
//        billingClient.startConnection(object : BillingClientStateListener {
//            override fun onBillingSetupFinished(result: BillingResult) {
//                if (result.responseCode == BillingResponseCode.OK) {
//                    productsAvailable()
//                    productPurchased()
//                }
//            }
//
//            override fun onBillingServiceDisconnected() {
//                context.toast(R.string.lb_toast_purchase_disconnect)
//            }
//        })
//    }
//
//    fun launchBillingFlow(activity: Activity, productDetails: ProductDetails) {
//        val params = BillingFlowParams
//            .ProductDetailsParams
//            .newBuilder()
//            .setProductDetails(productDetails)
//
//        if (productDetails.productType == ProductType.SUBS) {
//            val tokenSubs = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
//            tokenSubs?.let {
//                params.setOfferToken(tokenSubs)
//            } ?: {
//                context.toast(R.string.lb_toast_purchase_error)
//            }
//        }
//
//        val flowParams = BillingFlowParams.newBuilder()
//            .setProductDetailsParamsList(listOf(params.build())).build()
//
//        billingClient.launchBillingFlow(activity, flowParams)
//    }
//
//    fun productPurchased() {
//        if (!billingClient.isReady) {
//            context.toast(R.string.lb_toast_purchase_not_ready)
//            return
//        }
//
//        billingClient.queryPurchasesAsync(
//            QueryPurchasesParams.newBuilder()
//                .setProductType(ProductType.SUBS)
//                .build()
//        ) { blResult, purchases ->
//            if (purchases.isNotEmpty()) {
//                val hasValidSub = purchases.any {
//                    it.purchaseState == PurchaseState.PURCHASED
//                }
//                Log.d(TAG, "Has valid sub: $hasValidSub")
//                PurchasePrefs(context).hasPremium = hasValidSub
//            } else {
//                PurchasePrefs(context).hasPremium = false
//            }
//        }
//
//        billingClient.queryPurchasesAsync(
//            QueryPurchasesParams.newBuilder()
//                .setProductType(ProductType.INAPP)
//                .build()
//        ) { blResult, purchases ->
//            if (purchases.isNotEmpty()) {
//                val hasValidSub = purchases.any {
//                    it.purchaseState == PurchaseState.PURCHASED
//                }
//                Log.d(TAG, "Has valid in app: $hasValidSub")
//                PurchasePrefs(context).hasPremium = hasValidSub
//            } else {
//                PurchasePrefs(context).hasPremium = false
//            }
//        }
//    }
//
//    fun findProductDetailsById(productId: String): ProductDetails? {
//        return _productDetails.value?.firstOrNull {
//            it.productId == productId
//        }
//    }
//
//    private fun handlePurchase(blResult: BillingResult, purchases: List<Purchase>?) {
//        if (blResult.responseCode != BillingResponseCode.OK) {
//            context.toast(R.string.lb_toast_purchase_error)
//            _purchaseStatus.postValue(false)
//            return
//        }
//
//        if (purchases.isNullOrEmpty()) {
//            context.toast(R.string.lb_toast_purchase_error)
//            _purchaseStatus.postValue(false)
//            return
//        }
//
//        purchases.forEach { purchase ->
//            if (purchase.purchaseState == PurchaseState.PURCHASED) {
//                if (!purchase.isAcknowledged) {
//                    val ackParams = AcknowledgePurchaseParams
//                        .newBuilder()
//                        .setPurchaseToken(purchase.purchaseToken)
//                        .build()
//
//                    billingClient.acknowledgePurchase(ackParams) { ackResult ->
//                        if (ackResult.responseCode == BillingResponseCode.OK) {
//                            PurchasePrefs(context).hasPremium = true
//                            _purchaseStatus.postValue(true)
//                        }
//                    }
//                } else {
//                    PurchasePrefs(context).hasPremium = true
//                    _purchaseStatus.postValue(true)
//                }
//            } else {
//                PurchasePrefs(context).hasPremium = false
//                _purchaseStatus.postValue(false)
//            }
//        }
//    }
//
//    private fun productsAvailable() {
//        val loadProducts = mutableListOf<ProductDetails>()
//        val inAppProduct = ImmutableList.of(
//            QueryProductDetailsParams.Product
//                .newBuilder().setProductId(IN_APP_LIFE_TIME)
//                .setProductType(ProductType.INAPP)
//                .build()
//        )
//
//        val inAppParams = QueryProductDetailsParams.newBuilder()
//            .setProductList(inAppProduct)
//            .build()
//
//        billingClient.queryProductDetailsAsync(inAppParams) { blResult, productDetails ->
//            if (blResult.responseCode == BillingResponseCode.OK && productDetails.isNotEmpty()) {
//                loadProducts.addAll(productDetails)
//            } else {
//                Log.d(TAG, "Failed to query in app product details")
//            }
//        }
//
//        val subsProduct = ImmutableList.of(
//            QueryProductDetailsParams.Product
//                .newBuilder().setProductId(SUBS_PREMIUM_WEEKLY)
//                .setProductType(ProductType.SUBS)
//                .build(),
//
//            QueryProductDetailsParams.Product
//                .newBuilder().setProductId(SUBS_PREMIUM_YEARLY)
//                .setProductType(ProductType.SUBS)
//                .build()
//        )
//
//        val subsParams = QueryProductDetailsParams.newBuilder()
//            .setProductList(subsProduct)
//            .build()
//
//        billingClient.queryProductDetailsAsync(subsParams) { blResult, productDetails ->
//            if (blResult.responseCode == BillingResponseCode.OK && productDetails.isNotEmpty()) {
//                loadProducts.addAll(productDetails)
//            } else {
//                Log.d(TAG, "Failed to query subs product details")
//            }
//        }
//        _productDetails.postValue(loadProducts)
//    }
//}