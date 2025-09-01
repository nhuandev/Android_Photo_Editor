package com.example.appphotointern.ui.purchase

import android.os.Bundle
import android.util.Log
import com.android.billingclient.api.*
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding

class PurchaseActivity2 : BaseActivity(), PurchasesUpdatedListener {
    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }
    private lateinit var billingClient: BillingClient

    private var weeklyProduct: ProductDetails? = null
    private var yearlyProduct: ProductDetails? = null
    private var lifetimeProduct: ProductDetails? = null

    companion object {
        const val PREMIUM_WEEKLY_ID = "premium_weekly"
        const val PREMIUM_YEARLY_ID = "premium_yearly"
        const val PREMIUM_LIFETIME_ID = "lifetime_premium"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupBillingClient()
        initEvent()
    }

    /** 1. Khởi tạo BillingClient */
    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAllProducts()
                } else {
                    Log.e("BillingDebug", "❌ Setup error: ${billingResult.debugMessage}")
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.w("BillingDebug", "⚠️ Billing service disconnected, sẽ retry sau")
            }
        })
    }

    /** 2. Query cả 3 sản phẩm */
    private fun queryAllProducts() {
        Log.d("BillingDebug", "👉 Bắt đầu queryAllProducts...")

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_WEEKLY_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_YEARLY_ID)
                .setProductType(BillingClient.ProductType.SUBS)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_LIFETIME_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { result, details ->
            Log.d("BillingDebug", "👉 Callback queryProductDetailsAsync chạy rồi")
            Log.d("BillingDebug", "Response: ${result.responseCode}, msg=${result.debugMessage}")

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d("BillingDebug", "Products Count: ${details.size}")
                details.forEach { product ->
                    Log.d("BillingDebug", "✅ SUCCESS! ${product.productId} - ${product.title}")
                }
            } else {
                Log.e("BillingDebug", "❌ Query error: ${result.debugMessage}")
            }
        }
    }

    /** 3. Mở purchase flow theo product được chọn */
    private fun launchPurchase(product: ProductDetails?) {
        product?.let {
            val productDetailsParamsList = listOf(
                BillingFlowParams.ProductDetailsParams.newBuilder()
                    .setProductDetails(it)
                    .build()
            )

            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(productDetailsParamsList)
                .build()

            billingClient.launchBillingFlow(this, billingFlowParams)
        } ?: Log.w("BillingDebug", "⚠️ Product chưa load xong")
    }

    /** 4. Listener nhận kết quả purchase */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                Log.d("BillingDebug", "🎉 Purchase thành công: ${purchase.products}")
                acknowledgePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.d("BillingDebug", "❌ User canceled")
        } else {
            Log.e("BillingDebug", "❌ Purchase error: ${billingResult.debugMessage}")
        }
    }

    /** 5. Acknowledge để hoàn tất thanh toán */
    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.acknowledgePurchase(params) { result ->
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingDebug", "✅ Purchase acknowledged: ${purchase.products}")
                }
            }
        }
    }

    /** 6. Event UI */
    private fun initEvent() {
        binding.apply {
            btnCheckMonthly.setOnClickListener { launchPurchase(weeklyProduct) }
            btnCheckYearly.setOnClickListener { launchPurchase(yearlyProduct) }
            btnCheckLife.setOnClickListener { launchPurchase(lifetimeProduct) }

            btnContinue.setOnClickListener {
                when {
                    btnCheckMonthly.isChecked -> launchPurchase(weeklyProduct)
                    btnCheckYearly.isChecked -> launchPurchase(yearlyProduct)
                    btnCheckLife.isChecked -> launchPurchase(lifetimeProduct)
                }
            }

            btnCloseOverlay.setOnClickListener { finish() }
        }
    }
}
