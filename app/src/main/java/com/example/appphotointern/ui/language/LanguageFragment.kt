package com.example.appphotointern.ui.language

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentLanguageBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.Language

class LanguageFragment : DialogFragment() {
    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private var selectedLanguage: Language? = null
    private lateinit var languageAdapter: LanguageAdapter
    private val languageViewModel: LanguageViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initEvent()
        initObserver()
    }

    private fun initUI() {
        languageAdapter = LanguageAdapter(emptyList()) {
            selectedLanguage = it
        }
        binding.recLanguage.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recLanguage.adapter = languageAdapter
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    private fun initEvent() {
        binding.btnDoneLanguage.setOnClickListener {
            val selected = languageAdapter.getSelectedLanguage()
            selected?.let {
                LanguageManager.setLanguage(requireContext(), selected.code)
                LanguageManager.applyLanguage(requireContext(), selected.code)
                requireActivity().recreate()
                dismiss()
            } ?: run {
                requireContext().toast(R.string.lb_please_select_language)
            }
        }
    }

    private fun initObserver() {
        languageViewModel.languages.observe(viewLifecycleOwner) { languages ->
            languageAdapter.updateLanguage(languages)
        }
    }
}