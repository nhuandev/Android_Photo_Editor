package com.example.appphotointern.ui.edit.tools.text

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.appphotointern.ui.edit.tools.text.tool.color.ColorFragment
import com.example.appphotointern.ui.edit.tools.text.tool.font.FontFragment
import com.example.appphotointern.common.TEXT_COLOR
import com.example.appphotointern.common.TEXT_FONT

class TextAdapterPager(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun createFragment(position: Int): Fragment {
        return when(position) {
            TEXT_COLOR -> ColorFragment()
            TEXT_FONT -> FontFragment()
            else -> ColorFragment()
        }
    }

    override fun getItemCount(): Int {
        return 2
    }
}