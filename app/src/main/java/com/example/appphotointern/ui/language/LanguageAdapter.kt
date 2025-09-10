package com.example.appphotointern.ui.language

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.databinding.ItemLanguageBinding
import com.example.appphotointern.models.Language

class LanguageAdapter(
    private var languageList: List<Language>,
    private val onItemClick: (Language) -> Unit
) : RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {
    private var selectedPosition: Int = RecyclerView.NO_POSITION

    inner class LanguageViewHolder(private val binding: ItemLanguageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(language: Language, isSelected: Boolean) {
            binding.imgFlag.setImageResource(language.flagRes)
            binding.tvLanguage.text = language.name
            binding.radioButton.isChecked = isSelected

            binding.radioButton.setOnClickListener {
                updateSelectedPosition(adapterPosition)
                onItemClick(language)
            }

            binding.root.setOnClickListener {
                updateSelectedPosition(adapterPosition)
                onItemClick(language)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguageViewHolder {
        val binding =
            ItemLanguageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LanguageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        val language = languageList[position]
        holder.bind(language, position == selectedPosition)
    }

    override fun getItemCount() = languageList.size

    private fun updateSelectedPosition(position: Int) {
        if (selectedPosition == position) return

        val previousSelectedPosition = selectedPosition
        selectedPosition = position

        if (previousSelectedPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousSelectedPosition)
        }
        notifyItemChanged(selectedPosition)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateLanguage(newLanguageList: List<Language>) {
        languageList = newLanguageList
        notifyDataSetChanged()
    }

    fun getSelectedLanguage(): Language? {
        return if (selectedPosition != RecyclerView.NO_POSITION) {
            languageList[selectedPosition]
        } else null
    }

    fun setSelectedLanguage(currentLanguageCode: String) {
        val index = languageList.indexOfFirst { it.code == currentLanguageCode }
        if (index != -1) {
            val oldPos = selectedPosition
            selectedPosition = index
            if (oldPos != RecyclerView.NO_POSITION) {
                notifyItemChanged(oldPos)
            } else {
                notifyItemChanged(selectedPosition)
            }
        }
    }
}