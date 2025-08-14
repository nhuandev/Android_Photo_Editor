package com.example.appphotointern.ui.edit

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ItemEditBinding
import com.example.appphotointern.models.ToolType
import com.example.appphotointern.utils.FEATURE_CROP
import com.example.appphotointern.utils.FEATURE_DRAW
import com.example.appphotointern.utils.FEATURE_FILTER
import com.example.appphotointern.utils.FEATURE_FRAME
import com.example.appphotointern.utils.FEATURE_STICKER
import com.example.appphotointern.utils.FEATURE_TEXT

class EditAdapter(
    private val onItemSelected: OnItemSelected
) : RecyclerView.Adapter<EditAdapter.ViewHolder>() {

    private val listTool: MutableList<ToolModel> = mutableListOf()
    private var selectedPosition = -1

    inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    interface OnItemSelected {
        fun onItemSelected(toolType: ToolType)
    }

    class ViewHolder(val binding: ItemEditBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(toolMode: ToolModel, isSelected: Boolean) {
            binding.apply {
                imgToolIcon.setImageResource(toolMode.mToolIcon)
                txtTool.text = toolMode.mToolName

                if (isSelected) {
                    root.setBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            android.R.color.white
                        )
                    )
                    imgToolIcon.setColorFilter(
                        ContextCompat.getColor(
                            root.context,
                            android.R.color.black
                        )
                    )
                    txtTool.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            android.R.color.black
                        )
                    )
                } else {
                    root.setBackgroundColor(Color.TRANSPARENT)
                    imgToolIcon.clearColorFilter()
                    txtTool.setTextColor(
                        ContextCompat.getColor(
                            root.context,
                            android.R.color.white
                        )
                    )
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemEditBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val toolMode = listTool[position]
        holder.bind(toolMode, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val oldPos = selectedPosition
            selectedPosition = position
            notifyItemChanged(oldPos)
            notifyItemChanged(position)
            onItemSelected.onItemSelected(toolMode.mToolType)
        }
    }

    override fun getItemCount(): Int = listTool.size

    init {
        listTool.add(ToolModel(FEATURE_CROP, R.mipmap.ic_tools, ToolType.CROP))
        listTool.add(ToolModel(FEATURE_FRAME, R.mipmap.ic_crop, ToolType.FRAME))
        listTool.add(ToolModel(FEATURE_STICKER, R.mipmap.ic_stickers, ToolType.STICKER))
        listTool.add(ToolModel(FEATURE_FILTER, R.mipmap.ic_filter, ToolType.FILTER))
        listTool.add(ToolModel(FEATURE_TEXT, R.mipmap.ic_text, ToolType.TEXT))
        listTool.add(ToolModel(FEATURE_DRAW, R.mipmap.ic_draw, ToolType.DRAW))
    }
}
