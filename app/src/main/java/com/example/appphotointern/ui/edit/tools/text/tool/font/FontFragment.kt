package com.example.appphotointern.ui.edit.tools.text.tool.font

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appphotointern.databinding.FragmentFontBinding
import com.example.appphotointern.ui.edit.EditViewModel
import com.example.appphotointern.ui.edit.tools.text.TextViewModel

class FontFragment : Fragment() {
    private var _binding: FragmentFontBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TextViewModel by activityViewModels()
    private val editViewModel: EditViewModel by activityViewModels()
    private lateinit var fontAdapter: FontAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentFontBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initObserver()
    }

    private fun initUI() {
        binding.recFonts.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            fontAdapter = FontAdapter(emptyList()) { font ->
                editViewModel.selectFont(font)
            }
            adapter = fontAdapter
        }
    }

    private fun initObserver() {
        viewModel.fonts.observe(viewLifecycleOwner) { fonts ->
            fontAdapter.updateFont(fonts)
        }
    }
}