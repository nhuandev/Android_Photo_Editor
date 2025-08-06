package com.example.appphotointern.ui.edit.tools.draw

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentToolDrawBinding
import com.example.appphotointern.models.ToolDraw
import com.example.appphotointern.views.DrawOnImageView
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape

class DrawToolFragment(
    private val drawOnImageView: DrawOnImageView
) : Fragment() {
    private var _binding: FragmentToolDrawBinding? = null
    private val binding get() = _binding!!

    private var selectedColor: Int = Color.BLACK

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentToolDrawBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initEvent()
    }

    private fun initEvent() {
        binding.apply {
            btnPen.setOnClickListener {
                btnPen.setBackgroundResource(R.drawable.border_view)
                btnEraser.setBackgroundResource(0)
                drawOnImageView.setToolDraw(ToolDraw.PEN)
                showDialogChangSize(ToolDraw.PEN)
                openColorPicker()
            }

            btnEraser.setOnClickListener {
                btnEraser.setBackgroundResource(R.drawable.border_view)
                btnPen.setBackgroundResource(0)
                drawOnImageView.setToolDraw(ToolDraw.ERASER)
                showDialogChangSize(ToolDraw.ERASER)
            }
        }
    }

    private fun openColorPicker() {
        ColorPickerDialog
            .Builder(requireContext())
            .setTitle(R.string.lb_choose_color)
            .setColorShape(ColorShape.CIRCLE)
            .setDefaultColor(selectedColor.toInt())
            .setColorListener { color, _ ->
                selectedColor = color
                val currentTool = ToolDraw.PEN
                when (currentTool) {
                    ToolDraw.PEN -> {
                        drawOnImageView.setPenColor(color)
                    }

                    else -> {}
                }
            }
            .show()
    }

    @SuppressLint("StringFormatInvalid")
    private fun showDialogChangSize(tool: ToolDraw) {
        val dialog = layoutInflater.inflate(R.layout.dialog_eraser_size, null)
        val seekBar = dialog.findViewById<SeekBar>(R.id.seekBarSize)
        val tvSizeValue = dialog.findViewById<TextView>(R.id.tvSizeValue)
        seekBar.progress = 50
        tvSizeValue.text = getString(R.string.lb_size_default)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val value = progress.coerceAtLeast(1)
                tvSizeValue.text = "Size: $value"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.lb_choose_size)
            .setView(dialog)
            .setPositiveButton(R.string.lb_ok) { dialog, _ ->
                if (tool == ToolDraw.ERASER) {
                    tvSizeValue.text = getString(R.string.lb_size, seekBar.progress)
                    drawOnImageView.setSize(seekBar.progress.toFloat(), ToolDraw.ERASER)
                } else if (tool == ToolDraw.PEN) {
                    tvSizeValue.text = getString(R.string.lb_size, seekBar.progress)
                    drawOnImageView.setSize(seekBar.progress.toFloat(), ToolDraw.PEN)
                }
            }
            .setNegativeButton(R.string.lb_cancel) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}