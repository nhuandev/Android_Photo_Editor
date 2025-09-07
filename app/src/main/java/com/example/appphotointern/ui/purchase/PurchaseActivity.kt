package com.example.appphotointern.ui.purchase

import android.os.Bundle
import android.util.Log
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding
import androidx.lifecycle.Observer
import com.android.billingclient.api.ProductDetails
import com.example.appphotointern.R
import com.example.appphotointern.common.PURCHASED
import com.example.appphotointern.extention.toast
import com.example.appphotointern.ui.purchase.BillingManager.Companion.IN_APP_LIFE_TIME
import com.example.appphotointern.ui.purchase.BillingManager.Companion.SUBS_PREMIUM_WEEKLY
import com.example.appphotointern.ui.purchase.BillingManager.Companion.SUBS_PREMIUM_YEARLY
import org.greenrobot.eventbus.EventBus

class PurchaseActivity : BaseActivity() {
    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }
    private lateinit var billingManager: BillingManager
    private var selectedPlanId: String = SUBS_PREMIUM_WEEKLY
    private var cachedProducts: List<ProductDetails> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        billingManager = BillingManager(this)
        billingManager.startBillingConnect()
        billingManager.productDetails.observe(this, Observer { list ->
            cachedProducts = list ?: emptyList()
        })

        billingManager.purchaseStatus.observe(this, Observer { success ->
            Log.d("STATE", "$success")
            if (success == true) {
                EventBus.getDefault().post(PURCHASED)
                finish()
            } else {
                toast(R.string.lb_toast_purchase_fail)
            }
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
                selectedPlanId = SUBS_PREMIUM_WEEKLY
            }

            btnCheckYearly.setOnClickListener {
                btnCheckWeekly.isChecked = false
                btnCheckYearly.isChecked = true
                btnCheckLife.isChecked = false
                selectedPlanId = SUBS_PREMIUM_YEARLY
            }

            btnCheckLife.setOnClickListener {
                btnCheckWeekly.isChecked = false
                btnCheckYearly.isChecked = false
                btnCheckLife.isChecked = true
                selectedPlanId = IN_APP_LIFE_TIME
            }

            btnContinue.setOnClickListener {
                val details = cachedProducts.firstOrNull { it.productId == selectedPlanId }
                    ?: billingManager.findProductDetailsById(selectedPlanId)

                details?.let {
                    billingManager.launchBillingFlow(this@PurchaseActivity, it)
                }
            }

            btnCloseOverlay.setOnClickListener { finish() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        billingManager.disconnect()
    }
}