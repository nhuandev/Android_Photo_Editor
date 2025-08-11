package com.example.appphotointern.ui.edit.tools.text.tool.font

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.databinding.ItemTextFontBinding

class FontAdapter(
    private var fonts: List<String>,
    private val onFontSelected: (String) -> Unit
) : RecyclerView.Adapter<FontAdapter.FontViewHolder>() {
    inner class FontViewHolder(private val binding: ItemTextFontBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(font: String) {
            binding.apply {
                tvFont.text = font.substringBeforeLast(".")
                val typeface = Typeface.createFromAsset(itemView.context.assets, "fonts/$font")
                tvFont.typeface = typeface
                root.setOnClickListener {
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
        holder.bind(font)
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