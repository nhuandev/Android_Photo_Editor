package com.example.appphotointern.ui.album

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityAlbumBinding
import com.example.appphotointern.ui.main.MainAdapter
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
                    val previewFragment = PreviewFragment.newInstance(uri.toString())
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentNavHost, previewFragment)
                        .addToBackStack(null)
                        .commit()
                }
            )
            addItemDecoration(MainAdapter.SpaceItemDecoration(10))
            layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
            adapter = adapterGallery
        }
    }

    private fun initObserver() {
        viewModel.imageUri.observe(this) { images ->
            adapterGallery.updateData(images)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}