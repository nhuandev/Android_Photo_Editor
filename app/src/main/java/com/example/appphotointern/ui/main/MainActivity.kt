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
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityMainBinding
import com.example.appphotointern.databinding.NavHeaderBinding
import com.example.appphotointern.extention.isHasPermission
import com.example.appphotointern.ui.album.AlbumActivity
import com.example.appphotointern.ui.camera.CameraFragment
import com.example.appphotointern.ui.language.LanguageFragment
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.ui.purchase.FragmentPurchase
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.extention.toast
import com.example.appphotointern.utils.CustomDialog
import com.example.appphotointern.utils.KEY_BANNER
import com.example.appphotointern.utils.KEY_BANNER_IMAGE_URL
import com.example.appphotointern.utils.KEY_BANNER_MESSAGE
import com.example.appphotointern.utils.KEY_BANNER_TITLE
import com.example.appphotointern.utils.KEY_SHOW_BANNER
import com.example.appphotointern.utils.TAG_FEATURE_ALBUM
import com.example.appphotointern.utils.TAG_FEATURE_BACKGROUND
import com.example.appphotointern.utils.TAG_FEATURE_CAMERA
import com.example.appphotointern.utils.TAG_FEATURE_EDIT
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.json.JSONObject

class MainActivity : BaseActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val remoteConfig by lazy { Firebase.remoteConfig }
    private val viewModel: MainViewModel by viewModels()
    private lateinit var adapterHome: MainAdapter

    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
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
                    .replace(R.id.fragment_camera, previewFragment)
                    .addToBackStack(null)
                    .commit()
                binding.fragmentCamera.visibility = View.VISIBLE
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
                        .replace(R.id.fragment_camera, CameraFragment())
                        .addToBackStack(null)
                        .commit()
                    binding.fragmentCamera.visibility = View.VISIBLE
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
        remoteConfigBanner()
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
                            .replace(R.id.fragment_camera, CameraFragment())
                            .addToBackStack(null)
                            .commit()
                        binding.fragmentCamera.visibility = View.VISIBLE
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

        binding.drawerMain.itemIconTintList = null
    }

    private fun initEvent() {
        binding.apply {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            btnMenu.setOnClickListener {
                drawerLayout.openDrawer(GravityCompat.START)
            }

            drawerMain.setNavigationItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_menu_premium -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_camera, FragmentPurchase())
                            .addToBackStack(null)
                            .commit()
                        fragmentCamera.visibility = View.VISIBLE
                    }

                    R.id.nav_menu_album -> {
                        if (!isHasPermission(storagePermission)) {
                            requestStoragePermissionLauncher.launch(storagePermission)
                        } else {
                            val intent = Intent(this@MainActivity, AlbumActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    R.id.nav_menu_language -> {
                        val languageFragment = LanguageFragment()
                        languageFragment.show(supportFragmentManager, languageFragment.tag)
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
        viewModel.loadFeatures(this)
        viewModel.features.observe(this) { features ->
            adapterHome.updateFeatures(features)
        }

        viewModel.loading.observe(this) { isLoading ->
            binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun remoteConfigBanner() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayBanner()
                } else {
                    toast(R.string.toast_load_fail)
                }
            }

        remoteConfig.addOnConfigUpdateListener(object : ConfigUpdateListener {
            override fun onUpdate(configUpdate: ConfigUpdate) {
                if (configUpdate.updatedKeys.contains(KEY_BANNER)) {
                    remoteConfig.activate().addOnCompleteListener {
                        displayBanner()
                    }
                }
            }

            override fun onError(error: FirebaseRemoteConfigException) {
                Log.e("RemoteConfig", "Config update error: ${error.message}")
            }
        })
    }

    private fun displayBanner() {
        val bannerJson = remoteConfig.getString(KEY_BANNER)
        if (bannerJson.isNotEmpty()) {
            try {
                val banner = JSONObject(bannerJson)
                val show = banner.getBoolean(KEY_SHOW_BANNER)
                if (show) {
                    CustomDialog().showBannerUI(
                        this@MainActivity,
                        title = banner.getString(KEY_BANNER_TITLE),
                        message = banner.getString(KEY_BANNER_MESSAGE),
                        imageUrl = banner.getString(KEY_BANNER_IMAGE_URL),
                        onClick = {

                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("RemoteConfig", "Parse error: ${e.message}")
            }
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