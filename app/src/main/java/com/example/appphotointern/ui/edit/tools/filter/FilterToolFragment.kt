package com.example.appphotointern.ui.edit.tools.filter

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.appphotointern.databinding.FragmentToolFilterBinding
import com.example.appphotointern.models.FilterType
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.views.DrawOnImageView
import com.google.android.material.slider.Slider

class FilterToolFragment(
    private val drawImageView: DrawOnImageView,
    private val editViewModel: EditViewModel
) : Fragment() {
    private var _binding: FragmentToolFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterAdapter: FilterAdapter
    private lateinit var originalBitmap: Bitmap

    private var currentFilterType: FilterType = FilterType.NONE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserver()
    }

    private fun initUI() {
        binding.apply {
            drawImageView.post {
                originalBitmap = drawImageView.getInitBmp() ?: return@post
            }
            filterAdapter = FilterAdapter(emptyList()) { filter ->
                applyFilter(filter.type)
            }
            rvFilter.adapter = filterAdapter
            seekbarFilter.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (!fromUser) return
                    when (currentFilterType) {
                        FilterType.BRIGHTNESS -> {
                            val brightnessValue = progress - 100f // range: -100 -> 100
                            editViewModel.applyFilter(
                                originalBitmap,
                                FilterType.BRIGHTNESS,
                                brightnessValue
                            )
                        }

                        FilterType.CONTRAST -> {
                            val contrastValue = progress / 50f // range: 0.0 -> 2.0
                            editViewModel.applyFilter(
                                originalBitmap,
                                FilterType.CONTRAST,
                                contrastValue
                            )
                        }

                        else -> {
                        }
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })

//            seekbarFilter.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
//                override fun onStartTrackingTouch(slider: Slider) {
//                    TODO("Not yet implemented")
//                }
//
//                override fun onStopTrackingTouch(slider: Slider) {
//                    TODO("Not yet implemented")
//                }
//            })
        }
    }

    private fun initObserver() {
        editViewModel.filters.observe(viewLifecycleOwner) { filters ->
            filterAdapter.updateFilters(filters)
        }

        editViewModel.filteredBitmap.observe(viewLifecycleOwner) { filteredBitmap ->
            drawImageView.applyFilterOnImage { filteredBitmap }
        }
    }

    private fun applyFilter(filterType: FilterType?) {
        if (!::originalBitmap.isInitialized || filterType == null) return
        currentFilterType = filterType

        binding.seekbarFilter.progress = when (filterType) {
            FilterType.BRIGHTNESS -> 100 // 0 brightness
            FilterType.CONTRAST -> 100 // scale 1.0
            else -> 0
        }

        if (filterType == FilterType.NONE) {
            drawImageView.applyFilterOnImage { originalBitmap }
            return
        }
        editViewModel.applyFilter(originalBitmap, filterType)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}