package com.example.appphotointern.ui.purchase

import android.os.Bundle
import com.example.appphotointern.R
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityPurchaseBinding

class PurchaseActivity : BaseActivity() {
    private val binding by lazy { ActivityPurchaseBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            btnCheckFree.setImageResource(R.drawable.ic_check_circle)
            btnCheckFee.setImageResource(R.drawable.ic_uncheck_circle)

            lyFree.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_check_circle)
                btnCheckFee.setImageResource(R.drawable.ic_uncheck_circle)
            }

            lyFee.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_uncheck_circle)
                btnCheckFee.setImageResource(R.drawable.ic_check_circle)
            }

            btnClose.setOnClickListener {
                finish()
            }
        }
    }
}