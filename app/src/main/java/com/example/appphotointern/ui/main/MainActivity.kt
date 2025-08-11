package com.example.appphotointern.ui.main

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
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
import com.example.appphotointern.ui.camera.CameraFragment
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.utils.TAG_FEATURE_ALBUM
import com.example.appphotointern.utils.TAG_FEATURE_BACKGROUND
import com.example.appphotointern.utils.TAG_FEATURE_CAMERA
import com.example.appphotointern.utils.TAG_FEATURE_EDIT
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private lateinit var adapterHome: MainAdapter
    private val viewModel: MainViewModel by viewModels()

    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private val cameraPermission: String = Manifest.permission.CAMERA
    private val storagePermission: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                val previewFragment = PreviewFragment.newInstance(it.toString())
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentNavHost, previewFragment)
                    .addToBackStack(null)
                    .commit()
                binding.fragmentNavHost.visibility = View.VISIBLE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        initPermissionLaunchers()
        initUI()
        initEvent()
        initObserver()
    }

    private fun initPermissionLaunchers() {
        requestCameraPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentNavHost, CameraFragment())
                        .addToBackStack(null)
                        .commit()
                    binding.fragmentNavHost.visibility = View.VISIBLE
                } else {
                    showPermissionDeniedDialog(cameraPermission)
                }
            }

        requestStoragePermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    pickImageLauncher.launch("image/*")
                } else {
                    showPermissionDeniedDialog(storagePermission)
                }
            }
    }

    private fun initUI() {
        adapterHome = MainAdapter(emptyList(), onItemClick = { feature ->
            when (feature.featureType) {
                TAG_FEATURE_EDIT -> {
                    if (!isHasPermission(storagePermission)) {
                        requestStoragePermissionLauncher.launch(storagePermission)
                    } else {
                        pickImageLauncher.launch("image/*")
                    }
                }

                TAG_FEATURE_CAMERA -> {
                    if (!isHasPermission(cameraPermission)) {
                        requestCameraPermissionLauncher.launch(cameraPermission)
                    } else {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentNavHost, CameraFragment())
                            .addToBackStack(null)
                            .commit()
                        binding.fragmentNavHost.visibility = View.VISIBLE
                    }
                }

                TAG_FEATURE_ALBUM -> {
                    if (!isHasPermission(storagePermission)) {
                        requestStoragePermissionLauncher.launch(storagePermission)
                    } else {
                        val intent = Intent(this, AlbumActivity::class.java)
                        startActivity(intent)
                    }
                }

                TAG_FEATURE_BACKGROUND -> {

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
                        if (!isHasPermission(storagePermission)) {
                            requestStoragePermissionLauncher.launch(storagePermission)
                        } else {
                            pickImageLauncher.launch("image/*")
                        }
                    }

                    R.id.nav_menu_bgr -> {

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

    private fun showPermissionDeniedDialog(permission: String) {
        if (shouldShowRequestPermissionRationale(permission)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.lb_storage_permission)
                .setMessage(R.string.lb_message_permission_gallery)
                .setPositiveButton(R.string.lb_allow) { _, _ ->
                    if (permission == cameraPermission) {
                        requestCameraPermissionLauncher.launch(cameraPermission)
                    } else {
                        requestStoragePermissionLauncher.launch(storagePermission)
                    }
                }
                .setNegativeButton(R.string.lb_cancel, null)
                .show()
        } else {
            AlertDialog.Builder(this)
                .setTitle(R.string.lb_storage_permission)
                .setMessage(R.string.lb_message_permission_gallery)
                .setPositiveButton(R.string.lb_setting) { _, _ -> gotoSetting() }
                .setNegativeButton(R.string.lb_cancel, null)
                .show()
        }
    }

    private fun gotoSetting() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}