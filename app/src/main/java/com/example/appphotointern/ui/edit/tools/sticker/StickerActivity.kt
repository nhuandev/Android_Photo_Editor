package com.example.appphotointern.ui.edit.tools.sticker

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityStickerBinding
import com.example.appphotointern.utils.BaseActivity
import com.example.appphotointern.utils.FEATURE_STICKER
import com.example.appphotointern.utils.RESULT_STICKER

class StickerActivity : BaseActivity() {
    private val binding by lazy { ActivityStickerBinding.inflate(layoutInflater) }
    private val viewModel by viewModels<StickerViewModel>()
    private lateinit var stickerAdapter: StickerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
        initObserver()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = getString(R.string.lb_sticker)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUI() {
        binding.apply {
            stickerAdapter = StickerAdapter(emptyList()) { sticker ->
                viewModel.downloadStickerToInternalStorage(sticker) { file ->
                    val intent = Intent().apply {
                        putExtra(FEATURE_STICKER, file?.absolutePath)
                    }
                    setResult(RESULT_STICKER, intent)
                    finish()
                }
            }
            recSticker.layoutManager = GridLayoutManager(this@StickerActivity, 4)
            recSticker.adapter = stickerAdapter
        }
    }

    private fun initObserver() {
        binding.apply {
            viewModel.stickers.observe(this@StickerActivity) { stickers ->
                stickerAdapter.updateStickers(stickers)
            }

            viewModel.loading.observe(this@StickerActivity) { isLoading ->
                progressSticker.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}