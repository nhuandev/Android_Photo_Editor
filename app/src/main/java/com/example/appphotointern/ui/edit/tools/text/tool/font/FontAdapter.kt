package com.example.appphotointern.ui.edit.tools.text.tool.font

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.databinding.ItemTextFontBinding

class FontAdapter(
    private var fonts: List<String>,
    private val onFontSelected: (String) -> Unit
) : RecyclerView.Adapter<FontAdapter.FontViewHolder>() {
    private var selectedPosition = -1

    inner class FontViewHolder(private val binding: ItemTextFontBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(font: String, isSelected: Boolean) {
            binding.apply {
                tvFont.text = font.substringBeforeLast(".")
                val typeface = Typeface.createFromAsset(itemView.context.assets, "fonts/$font")
                tvFont.typeface = typeface
                if (isSelected) {
                    root.setBackgroundColor(
                        ContextCompat.getColor(root.context, android.R.color.holo_orange_light)
                    )
                } else {
                    root.setBackgroundColor(Color.TRANSPARENT)
                }
                root.setOnClickListener {
                    val oldPos = selectedPosition
                    selectedPosition = bindingAdapterPosition
                    notifyItemChanged(oldPos)
                    notifyItemChanged(selectedPosition)
                    onFontSelected(font)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FontViewHolder {
        val binding =
            ItemTextFontBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FontViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FontViewHolder, position: Int) {
        val font = fonts[position]
        holder.bind(font, position == selectedPosition)
    }

    override fun getItemCount(): Int {
        return fonts.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateFont(newFonts: List<String>) {
        fonts = newFonts
        notifyDataSetChanged()
    }
}