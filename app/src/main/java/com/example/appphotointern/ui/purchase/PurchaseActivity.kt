package com.example.appphotointern.ui.purchase

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding
import androidx.lifecycle.Observer
import com.android.billingclient.api.ProductDetails
import com.azmobile.phonemirror.MainApplication
import com.example.appphotointern.R
import com.example.appphotointern.common.PURCHASED
import com.example.appphotointern.common.SPLASH_DELAY
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
        billingManager = (application as MainApplication).billingManager
        initUI()
        initEvents()
        initObserver()
    }

    private fun initUI() = with(binding) {
        fun selectPlan(weekly: Boolean, yearly: Boolean, life: Boolean, planId: String) {
            btnCheckWeekly.isChecked = weekly
            btnCheckYearly.isChecked = yearly
            btnCheckLife.isChecked = life
            selectedPlanId = planId
        }

        selectPlan(weekly = true, yearly = false, life = false, planId = SUBS_PREMIUM_WEEKLY)
        btnCheckWeekly.setOnClickListener {
            selectPlan(true, false, false, SUBS_PREMIUM_WEEKLY)
        }
        btnCheckYearly.setOnClickListener {
            selectPlan(false, true, false, SUBS_PREMIUM_YEARLY)
        }
        btnCheckLife.setOnClickListener {
            selectPlan(false, false, true, IN_APP_LIFE_TIME)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            btnCloseOverlay.visibility = View.VISIBLE
        }, SPLASH_DELAY)
    }

    private fun initEvents() {
        binding.apply {
            btnContinue.setOnClickListener {
                val details = cachedProducts.firstOrNull {
                    it.productId == selectedPlanId
                } ?: billingManager.findProductDetailsById(selectedPlanId)

                details?.let {
                    billingManager.launchBillingFlow(this@PurchaseActivity, it)
                }
            }

            btnCloseOverlay.setOnClickListener { finish() }
        }
    }

    private fun initObserver() {
        billingManager.productDetails.observe(this, Observer { list ->
            cachedProducts = list ?: emptyList()
        })

        billingManager.purchaseStatus.observe(this, Observer { success ->
            if (success == true) {
                toast(R.string.lb_toast_purchase_success)
                EventBus.getDefault().post(PURCHASED)
                finish()
            } else {
                toast(R.string.lb_toast_purchase_fail)
            }
        })
    }
}