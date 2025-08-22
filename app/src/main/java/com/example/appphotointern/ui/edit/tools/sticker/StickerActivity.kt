package com.example.appphotointern.ui.edit.tools.sticker

import android.os.Bundle
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityStickerBinding
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.ui.edit.tools.sticker.fragments.StickerViewPager
import com.example.appphotointern.utils.STICKER_BASIC
import com.example.appphotointern.utils.STICKER_CREATIVE
import com.example.appphotointern.utils.STICKER_FESTIVAL
import com.google.android.material.tabs.TabLayoutMediator

class StickerActivity : BaseActivity() {
    private val binding by lazy { ActivityStickerBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = getString(R.string.lb_sticker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUI() {
        val viewpager = StickerViewPager(this)
        binding.apply {
            viewpagerSticker.adapter = viewpager

            TabLayoutMediator(tlySticker, viewpagerSticker) { tab, position ->
                when (position) {
                    STICKER_BASIC -> tab.text = getString(R.string.lb_sticker_basic)
                    STICKER_FESTIVAL -> tab.text = getString(R.string.lb_sticker_festival)
                    STICKER_CREATIVE -> tab.text = getString(R.string.lb_sticker_creative)
                }
            }.attach()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}