package com.example.appphotointern.ui.edit.tools.text.tool.color

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentColorBinding
import com.example.appphotointern.ui.edit.EditViewModel
import kotlin.collections.iterator

class ColorFragment : Fragment() {
    private var _binding: FragmentColorBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentColorBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("UseKtx")
    private fun initUI() {
        binding.apply {
            val colorMap = mapOf(
                R.id.colorRed to Color.parseColor("#F44336"),
                R.id.colorPink to Color.parseColor("#E91E63"),
                R.id.colorBlue to Color.parseColor("#2196F3"),
                R.id.colorGreen to Color.parseColor("#4CAF50"),
                R.id.colorOrange to Color.parseColor("#FF9800"),
                R.id.colorYellow to Color.parseColor("#FFEB3B"),
                R.id.colorPurple to Color.parseColor("#9C27B0"),
                R.id.colorBrown to Color.parseColor("#795548"),
                R.id.colorGrey to Color.parseColor("#607D8B"),
                R.id.colorTeal to Color.parseColor("#00BCD4"),
                R.id.colorIndigo to Color.parseColor("#3F51B5"),
                R.id.colorLime to Color.parseColor("#CDDC39")
            )

            for ((viewId, color) in colorMap) {
                view?.findViewById<View>(viewId)?.setOnClickListener {
                    viewModel.selectColor(color)
                }
            }
        }
    }
}