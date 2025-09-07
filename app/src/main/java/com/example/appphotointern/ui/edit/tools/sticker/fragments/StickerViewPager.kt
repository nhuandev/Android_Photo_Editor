package com.example.appphotointern.ui.edit.tools.sticker.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appphotointern.common.STICKER_BASIC
import com.example.appphotointern.common.STICKER_CREATIVE
import com.example.appphotointern.common.STICKER_FESTIVAL

class StickerViewPager(fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            STICKER_BASIC -> BasicFragment()
            STICKER_FESTIVAL -> FestivalFragment()
            STICKER_CREATIVE -> CreativeFragment()
            else -> BasicFragment()
        }
    }

    override fun getItemCount(): Int {
        return 3
    }
}