package com.example.appphotointern.ui.edit.tools.filter

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.appphotointern.databinding.FragmentToolFilterBinding
import com.example.appphotointern.models.FilterType
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.utils.FilterManager
import com.example.appphotointern.views.DrawOnImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.getValue

class FilterToolFragment(
    private val drawImageView: DrawOnImageView
) : Fragment() {
    private var _binding: FragmentToolFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterAdapter: FilterAdapter
    private val editViewModel: EditViewModel by viewModels()
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
            drawImageView.post {
                originalBitmap = drawImageView.getInitBmp() ?: return@post
            }

            filterAdapter = FilterAdapter(emptyList()) { filter ->
                applyFilter(filter.type)
            }
            rvFilter.adapter = filterAdapter
        }
    }

    private fun applyFilter(filterType: FilterType?) {
        if (!::originalBitmap.isInitialized || filterType == null) return

        if (filterType == FilterType.NONE) {
            drawImageView.applyFilter {
                originalBitmap
            }
            return
        }

        binding.progressFilter.visibility = View.VISIBLE
        lifecycleScope.launch {
            val filteredBitmap = withContext(Dispatchers.Default) {
                when (filterType) {
                    FilterType.GRAYSCALE -> FilterManager.applyGrayscale(originalBitmap)
                    FilterType.SEPIA -> FilterManager.applySepia(originalBitmap)
                    FilterType.INVERT -> FilterManager.applyInvert(originalBitmap)
                    FilterType.BRIGHTNESS -> FilterManager.applyBrightness(originalBitmap, 100f)
                    FilterType.CONTRAST -> FilterManager.applyContrast(originalBitmap, 1.5f)
                    else -> originalBitmap
                }
            }

            drawImageView.applyFilter {
                filteredBitmap
            }
            binding.progressFilter.visibility = View.GONE
        }
    }

    private fun initObserver() {
        editViewModel.filters.observe(viewLifecycleOwner) { filters ->
            filterAdapter.updateFilters(filters)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}