package com.example.appphotointern.ui.analytics.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appphotointern.utils.ANALYTICS_FILTER
import com.example.appphotointern.utils.ANALYTICS_STICKER

class AnalyticsViewPager(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            ANALYTICS_FILTER -> FilterAnalyticsFragment()
            ANALYTICS_STICKER -> StickerAnalyticsFragment()
            else -> FilterAnalyticsFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}