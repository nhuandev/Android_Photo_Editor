package com.example.appphotointern.ui.analytics.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.common.LOAD_FAIL
import com.example.appphotointern.common.LOAD_SUCCESS
import com.example.appphotointern.databinding.FragmentAnalyticsFilterBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.ui.analytics.AnalyticsAdapter
import com.example.appphotointern.ui.analytics.AnalyticsViewModel

class FilterAnalyticsFragment : Fragment() {
    private var _binding: FragmentAnalyticsFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var analyticsAdapter: AnalyticsAdapter
    private val viewModel by viewModels<AnalyticsViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnalyticsFilterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.loadTopFilters()
        initUI()
        initObserver()
    }

    private fun initUI() {
        analyticsAdapter = AnalyticsAdapter(emptyList())
        binding.recAnalyticsFilter.layoutManager = LinearLayoutManager(requireContext())
        binding.recAnalyticsFilter.adapter = analyticsAdapter
    }

    private fun initObserver() {
        viewModel.notify.observe(viewLifecycleOwner) {
            if (it == LOAD_FAIL) {
                requireContext().toast(R.string.toast_load_fail)
                binding.tvError.visibility = View.VISIBLE
            }
        }

        viewModel.topFilters.observe(viewLifecycleOwner) {
            analyticsAdapter.setData(it)
        }

        viewModel.loading.observe(viewLifecycleOwner) {
            binding.progressFilter.visibility = if (it) View.VISIBLE else View.GONE
        }
    }
}