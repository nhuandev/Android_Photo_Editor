package com.example.appphotointern.ui.purchase

import android.os.Bundle
import com.example.appphotointern.R
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult

class PurchaseActivity : BaseActivity() {
    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }
    private lateinit var paymentSheet: PaymentSheet
    private var paymentIntentClientSecret: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51S0HF5ANwbG8U3iNCaGOiLDfYXwdQafO8NSNE7yUjtRyisk6KrboJqPkaleQtJapBwGYXAcGO1JjdwVwmYQlt3TT00cdgb6jqw"
        )

        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            lyFree.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_check_circle)
                btnCheckFee.setImageResource(R.drawable.ic_uncheck_circle)
            }

            lyFee.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_uncheck_circle)
                btnCheckFee.setImageResource(R.drawable.ic_check_circle)
            }

            btnContinue.setOnClickListener {
                paymentIntentClientSecret =
                    "pi_3S0I5TAm53fPVZQo0nsoXRnM_secret_OHIXNOpctfImEEvWOGPJqvwQE"

                paymentIntentClientSecret?.let { secret ->
                    paymentSheet.presentWithPaymentIntent(
                        secret,
                        PaymentSheet.Configuration("App Photo Premium")
                    )
                }
            }

            btnCloseOverlay.setOnClickListener { finish() }
        }
    }

    private fun onPaymentSheetResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                unlockFeatures(true)
            }

            is PaymentSheetResult.Canceled -> {
            }

            is PaymentSheetResult.Failed -> {
            }
        }
    }

    private fun unlockFeatures(premium: Boolean) {
        if (premium) {
        } else {
        }
    }
}
