package com.example.appphotointern.ui.purchase

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentPurchaseBinding

class FragmentPurchase : Fragment() {
    private var _binding: FragmentPurchaseBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPurchaseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            btnCheckFree.setImageResource(R.drawable.ic_check_circle)
            btnCheckFee.setImageResource(R.drawable.ic_uncheck_circle)

            lyFree.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_check_circle)
                btnCheckFee.setImageResource(R.drawable.ic_uncheck_circle)
            }

            lyFee.setOnClickListener {
                btnCheckFree.setImageResource(R.drawable.ic_uncheck_circle)
                btnCheckFee.setImageResource(R.drawable.ic_check_circle)
            }

            btnClose.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .remove(this@FragmentPurchase)
                    .commit()
            }
        }
    }
}