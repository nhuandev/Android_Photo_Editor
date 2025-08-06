package com.example.appphotointern.ui.edit.tools.text

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.appphotointern.databinding.ActivityTextBinding
import com.example.appphotointern.utils.FEATURE_TEXT
import com.example.appphotointern.utils.RESULT_TEXT

class TextActivity : AppCompatActivity() {
    private val binding by lazy { ActivityTextBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
        initEvent()
    }

    private fun initUI() {
        binding.apply {
            val currentText = intent.getStringExtra(FEATURE_TEXT)
            edtText.setText(currentText ?: "")
        }
    }

    private fun initEvent() {
        binding.apply {
            btnDone.setOnClickListener {
                val intent = Intent().apply {
                    putExtra(FEATURE_TEXT, edtText.text.toString())
                }
                setResult(RESULT_TEXT, intent)
                finish()
            }
        }
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.apply {
            title = null
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}