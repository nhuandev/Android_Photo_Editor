package com.example.appphotointern.ui.album

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityAlbumBinding
import com.example.appphotointern.ui.preview.PreviewFragment

class AlbumActivity : AppCompatActivity() {
    private val binding by lazy { ActivityAlbumBinding.inflate(layoutInflater) }
    private lateinit var adapterGallery: AlbumAdapter
    private val viewModel: AlbumViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setUpToolBar()
        initUI()
        initObserver()
    }

    private fun setUpToolBar() {
        setSupportActionBar(binding.toolBar)
        supportActionBar?.title = null
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initUI() {
        binding.recPhotoEdited.apply {
            adapterGallery = AlbumAdapter(
                emptyList(),
                onClick = { uri ->
                    Log.d("AlbumActivity", "Image clicked: $uri")
                    val previewFragment = PreviewFragment.newInstance(uri.toString())
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentNavHost, previewFragment)
                        .commit()
                }
            )
            layoutManager = GridLayoutManager(this@AlbumActivity, 4)
            adapter = adapterGallery
        }
    }

    private fun initObserver() {
        viewModel.imageUri.observe(this) { images ->
            adapterGallery.updateData(images)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressAlbum.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}