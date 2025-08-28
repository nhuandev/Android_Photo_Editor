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
import com.example.appphotointern.utils.AnalyticsManager
import com.example.appphotointern.utils.AnalyticsManager.LogEvent.EVENT_FILTER_SELECTED
import com.example.appphotointern.utils.AnalyticsManager.LogEvent.PARAM_FILTER_NAME
import com.example.appphotointern.utils.FireStoreManager
import com.example.appphotointern.views.ImageOnView

class FilterToolFragment() : Fragment() {
    private var _binding: FragmentToolFilterBinding? = null
    private val binding get() = _binding!!

    private var currentFilterType: FilterType = FilterType.NONE
    private lateinit var drawImageView: ImageOnView
    private lateinit var editViewModel: EditViewModel
    private lateinit var filterAdapter: FilterAdapter
    private lateinit var originalBitmap: Bitmap
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
            filterAdapter = FilterAdapter(emptyList()) { filter ->
                AnalyticsManager.logEvent(
                    EVENT_FILTER_SELECTED,
                    mapOf(PARAM_FILTER_NAME to filter.name)
                )
                FireStoreManager.tryIncrementFilter(filter.name)
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
                            val brightnessValue = progress - 100f
                            editViewModel.applyFilter(
                                originalBitmap,
                                FilterType.BRIGHTNESS,
                                brightnessValue
                            )
                        }

                        FilterType.CONTRAST -> {
                            val contrastValue = progress / 50f
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

            // Load filter on image preview
            drawImageView.post {
                // Get original bitmap
                originalBitmap = drawImageView.getInitBmp() ?: return@post
                editViewModel.loadFilter(originalBitmap)
            }
        }
    }

    private fun initObserver() {
        editViewModel.filters.observe(viewLifecycleOwner) { filters ->
            filterAdapter.updateFilters(filters)
        }

        editViewModel.filteredBitmap.observe(viewLifecycleOwner) { filteredBitmap ->
            drawImageView.applyFilterOnImage { filteredBitmap }
        }

        editViewModel.currentBitmap.observe(viewLifecycleOwner) { bm ->
            drawImageView.setImageBitmap(bm)
        }
    }

    private fun applyFilter(filterType: FilterType?) {
        if (!::originalBitmap.isInitialized || filterType == null) return
        currentFilterType = filterType

        binding.seekbarFilter.progress = when (filterType) {
            FilterType.BRIGHTNESS -> 100
            FilterType.CONTRAST -> 100
            else -> 0
        }

        if (filterType == FilterType.NONE) {
            drawImageView.applyFilterOnImage { originalBitmap }
            return
        }
        editViewModel.applyFilter(originalBitmap, filterType)
    }

    companion object {
        fun newInstance(): FilterToolFragment {
            return FilterToolFragment().apply {
                arguments = Bundle().apply {

                }
            }
        }
    }

    fun setDependencies(drawImageView: ImageOnView, editViewModel: EditViewModel) {
        this.drawImageView = drawImageView
        this.editViewModel = editViewModel
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}