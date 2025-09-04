package com.example.appphotointern.ui.purchase

import android.os.Bundle
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding
import com.example.appphotointern.utils.BillingManager
import com.example.appphotointern.utils.BillingManager.Companion.LIFETIME_ID
import com.example.appphotointern.utils.BillingManager.Companion.PREMIUM_WEEKLY_ID
import com.example.appphotointern.utils.BillingManager.Companion.PURCHASE_YEARLY_ID
import com.android.billingclient.api.ProductDetails
import androidx.lifecycle.Observer

class PurchaseActivity : BaseActivity() {
    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }
    private lateinit var billingManager: BillingManager
    private var selectedPlanId: String = PREMIUM_WEEKLY_ID
    private var cachedProducts: List<ProductDetails> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        billingManager = BillingManager(this)
        billingManager.startConnection()

        billingManager.productDetails.observe(this, Observer { list ->
            cachedProducts = list ?: emptyList()
        })

        billingManager.purchaseStatus.observe(this, Observer { success ->
            // You can navigate or show a success dialog here
            if (success == true) finish()
        })

        initEvents()
    }

    private fun initEvents() {
        binding.apply {
            btnCheckWeekly.isChecked = true
            btnCheckYearly.isChecked = false
            btnCheckLife.isChecked = false

            btnCheckWeekly.setOnClickListener {
                btnCheckWeekly.isChecked = true
                btnCheckYearly.isChecked = false
                btnCheckLife.isChecked = false
                selectedPlanId = PREMIUM_WEEKLY_ID
            }

            btnCheckYearly.setOnClickListener {
                btnCheckWeekly.isChecked = false
                btnCheckYearly.isChecked = true
                btnCheckLife.isChecked = false
                selectedPlanId = PURCHASE_YEARLY_ID
            }

            btnCheckLife.setOnClickListener {
                btnCheckWeekly.isChecked = false
                btnCheckYearly.isChecked = false
                btnCheckLife.isChecked = true
                selectedPlanId = LIFETIME_ID
            }

            btnContinue.setOnClickListener {
                val details = cachedProducts.firstOrNull { it.productId == selectedPlanId }
                    ?: billingManager.findProductDetailsById(selectedPlanId)
                if (details != null) {
                    billingManager.launchPurchaseFlow(this@PurchaseActivity, details)
                }
            }

            btnCloseOverlay.setOnClickListener { finish() }
        }
    }
}
