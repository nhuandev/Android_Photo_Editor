package com.example.appphotointern.ui.edit.tools.text.tool

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appphotointern.databinding.FragmentToolTextBinding
import com.example.appphotointern.ui.edit.tools.text.TextAdapterPager
import com.example.appphotointern.common.TEXT_COLOR
import com.example.appphotointern.common.TEXT_DATA_COLOR
import com.example.appphotointern.common.TEXT_DATA_FONT
import com.example.appphotointern.common.TEXT_FONT
import com.google.android.material.tabs.TabLayoutMediator

class TextToolFragment : Fragment() {
    private var _binding: FragmentToolTextBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentToolTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    private fun initUI() {
        binding.apply {
            val textPagerAdapter = TextAdapterPager(this@TextToolFragment)
            viewPagerText.adapter = textPagerAdapter

            TabLayoutMediator(tlText, viewPagerText) { tab, position ->
                when (position) {
                    TEXT_COLOR -> tab.text = TEXT_DATA_COLOR
                    TEXT_FONT -> tab.text = TEXT_DATA_FONT
                    else -> tab.text = TEXT_DATA_COLOR
                }
            }.attach()
        }
    }
}