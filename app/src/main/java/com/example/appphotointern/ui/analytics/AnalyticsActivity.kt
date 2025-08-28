package com.example.appphotointern.ui.analytics

import android.os.Bundle
import com.example.appphotointern.R
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.databinding.ActivityAnalyticsBinding
import com.example.appphotointern.ui.analytics.fragments.AnalyticsViewPager
import com.example.appphotointern.utils.ANALYTICS_FILTER
import com.example.appphotointern.utils.ANALYTICS_STICKER
import com.google.android.material.tabs.TabLayoutMediator

class AnalyticsActivity : BaseActivity() {
    private val binding by lazy { ActivityAnalyticsBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = getString(R.string.lb_analytics)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUI() {
        val viewpager = AnalyticsViewPager(this)
        binding.apply {
            viewPagerAnalytics.adapter = viewpager
            TabLayoutMediator(tlyAnalytics, viewPagerAnalytics) { tab, position ->
                when (position) {
                    ANALYTICS_FILTER -> tab.text = getString(R.string.lb_filter)
                    ANALYTICS_STICKER -> tab.text = getString(R.string.lb_sticker)
                }
            }.attach()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}