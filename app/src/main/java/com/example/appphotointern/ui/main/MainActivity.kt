package com.example.appphotointern.ui.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityMainBinding
import com.example.appphotointern.databinding.NavHeaderBinding
import com.example.appphotointern.extention.isHasPermission
import com.example.appphotointern.ui.album.AlbumActivity
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.utils.TAG_FEATURE_BACKGROUND
import com.example.appphotointern.utils.TAG_FEATURE_CAMERA
import com.example.appphotointern.utils.TAG_FEATURE_COLLAGE
import com.example.appphotointern.utils.TAG_FEATURE_EDIT

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mPermission = Manifest.permission.READ_EXTERNAL_STORAGE
    private lateinit var adapterHome: MainAdapter
    private val viewModel: MainViewModel by viewModels()

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initPermissionLauncher()
        if (!isHasReadPermission()) {
            requestReadPermission()
        }
        initUI()
        initEvent()
        initObserver()
    }

    private fun initUI() {
        adapterHome = MainAdapter(
            emptyList(),
            onItemClick = { feature ->
                when (feature.featureType) {
                    TAG_FEATURE_EDIT -> {
                        pickImageLauncher.launch("image/*")
                    }

                    TAG_FEATURE_COLLAGE -> {

                    }

                    TAG_FEATURE_BACKGROUND -> {

                    }

                    TAG_FEATURE_CAMERA -> {

                    }

                    else -> {

                    }
                }
            })

        binding.recFeature.apply {
            addItemDecoration(MainAdapter.SpaceItemDecoration(20))
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = adapterHome
        }
    }

    private fun initEvent() {
        binding.apply {
            btnMenu.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            drawerMain.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_menu_photo -> {
                        pickImageLauncher.launch("image/*")
                    }

                    R.id.nav_menu_bgr -> {

                    }

                    R.id.nav_menu_collage -> {

                    }

                    R.id.nav_menu_album -> {
                        if (!isHasReadPermission()) {
                            requestReadPermission()
                        } else {
                            val intent = Intent(this@MainActivity, AlbumActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    R.id.nav_menu_dev -> {

                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START)
                true
            }

            val navHeaderBinding = NavHeaderBinding.bind(drawerMain.getHeaderView(0))
            navHeaderBinding.btnCloseDraw.setOnClickListener {
                drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun initObserver() {
        viewModel.features.observe(this) { features ->
            adapterHome.updateFeatures(features)
        }
        viewModel.loading.observe(this) { isLoading ->
            binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val dialog = PreviewFragment.Companion.newInstance(it.toString())
                dialog.show(supportFragmentManager, "PreviewFragment")
            }
        }

    private fun initPermissionLauncher() {
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                when {
                    isGranted -> {

                    }

                    shouldShowRequestPermissionRationale(mPermission) -> {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.lb_storage_permission)
                            .setMessage(R.string.lb_message_permission_gallery)
                            .setPositiveButton(R.string.lb_allow) { _, _ ->
                                requestReadPermission()
                            }
                            .setNegativeButton(R.string.lb_cancel) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }

                    else -> {
                        AlertDialog.Builder(this)
                            .setTitle(R.string.lb_storage_permission)
                            .setMessage(R.string.lb_message_permission_gallery)
                            .setPositiveButton(R.string.lb_setting) { _, _ ->
                                gotoSetting()
                            }
                            .setNegativeButton(R.string.lb_cancel) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .create()
                            .show()
                    }
                }
            }
    }

    private fun gotoSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(
            intent
        )
    }

    private fun isHasReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return isHasPermission(permission)
    }

    private fun requestReadPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mPermission = Manifest.permission.READ_MEDIA_IMAGES
        }
        requestPermissionLauncher.launch(mPermission)
    }
}