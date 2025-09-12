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
import com.azmobile.phonemirror.MainApplication
import com.example.appphotointern.R
import com.example.appphotointern.databinding.ActivityMainBinding
import com.example.appphotointern.databinding.NavHeaderBinding
import com.example.appphotointern.extention.isHasPermission
import com.example.appphotointern.ui.album.AlbumActivity
import com.example.appphotointern.ui.camera.CameraFragment
import com.example.appphotointern.ui.language.LanguageFragment
import com.example.appphotointern.ui.preview.PreviewFragment
import com.example.appphotointern.ui.purchase.PurchaseActivity
import com.example.appphotointern.common.BaseActivity
import com.example.appphotointern.extention.toast
import com.example.appphotointern.utils.AdManager
import com.example.appphotointern.common.CustomDialog
import com.example.appphotointern.common.KEY_BANNER
import com.example.appphotointern.common.KEY_BANNER_IMAGE_URL
import com.example.appphotointern.common.KEY_BANNER_MESSAGE
import com.example.appphotointern.common.KEY_BANNER_TITLE
import com.example.appphotointern.common.KEY_SHOW_BANNER
import com.example.appphotointern.common.PURCHASED
import com.example.appphotointern.utils.PresenceManager
import com.example.appphotointern.common.TAG_FEATURE_ALBUM
import com.example.appphotointern.common.TAG_FEATURE_ANALYTICS
import com.example.appphotointern.common.TAG_FEATURE_CAMERA
import com.example.appphotointern.common.TAG_FEATURE_EDIT
import com.example.appphotointern.ui.analytics.AnalyticsActivity
import com.example.appphotointern.utils.NetworkReceiver
import com.example.appphotointern.utils.PurchasePrefs
import com.google.android.gms.ads.AdRequest
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

class MainActivity : BaseActivity() {
    val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val cameraPermission: String = Manifest.permission.CAMERA
    private val remoteConfig by lazy { Firebase.remoteConfig }
    private val mainViewModel: MainViewModel by viewModels()
    private val customDialog = CustomDialog()

    private lateinit var requestStoragePermissionLauncher: ActivityResultLauncher<String>
    private lateinit var requestCameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var networkReceiver: NetworkReceiver
    private lateinit var adapterHome: MainAdapter

    companion object {
        private var checkShowPurchase = false
    }

    private val storagePermission: String by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private val pickImageLauncher =
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
        AdManager.loadAdReward(this)
        AdManager.loadNative(this, binding.flNativeBanner)
        checkPremiumFlow()
        initPermissionLaunchers()
        initUI()
        initEvent()
        initObserver()
        checkShowBanner()
        checkPremium()
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
        networkReceiver = NetworkReceiver(this)
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

                TAG_FEATURE_ANALYTICS -> {
                    val hasPremium = PurchasePrefs(this).hasPremium
                    val isConnect = networkReceiver.isConnected()
                    val isCheckReward = AdManager.isCheckReward
                    Log.d("TAG", "initUI: $isCheckReward")
                    when {
                        isCheckReward || (isConnect && hasPremium) -> {
                            startActivity(Intent(this, AnalyticsActivity::class.java))
                        }

                        isConnect -> {
                            AdManager.showReward(this)
                        }

                        else -> {
                            toast(R.string.lb_toast_network_error)
                        }
                    }
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
                        val checkPremium = PurchasePrefs(this@MainActivity).hasPremium
                        Log.d("TAG", "initEvent: $checkPremium")
                        if (checkPremium) {
                            customDialog.showPremiumDialog(this@MainActivity)
                        } else {
                            startActivity(Intent(this@MainActivity, PurchaseActivity::class.java))
                        }
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
        mainViewModel.loadFeatures(this)
        mainViewModel.features.observe(this) { features ->
            adapterHome.updateFeatures(features)
        }

        mainViewModel.loading.observe(this) { isLoading ->
            binding.progressCircular.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun remoteConfigBanner() {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    displayBanner()
                } else {
                    toast(R.string.toast_load_fail)
                }
            }
    }

    fun displayBanner() {
        val bannerJson = remoteConfig.getString(KEY_BANNER)
        if (bannerJson.isNotEmpty()) {
            try {
                val banner = JSONObject(bannerJson)
                val show = banner.getBoolean(KEY_SHOW_BANNER)
                if (show) {
                    customDialog.showBannerUI(
                        this,
                        title = banner.getString(KEY_BANNER_TITLE),
                        message = banner.getString(KEY_BANNER_MESSAGE),
                        imageUrl = banner.getString(KEY_BANNER_IMAGE_URL),
                        onClick = {
                            val checkPremium = PurchasePrefs(this).hasPremium
                            if (checkPremium) customDialog.showPremiumDialog(this)
                            else startActivity(Intent(this, PurchaseActivity::class.java))
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

    private fun checkPremium() {
        val checkPremium = PurchasePrefs(this).hasPremium
        Log.d("TAG", "checkPremium: $checkPremium")
        if (checkPremium) {
            hideAds()
        } else {
            networkReceiver.observe(this) {
                if (it) showAds() else hideAds()
            }
        }
    }

    private fun showAds() {
        binding.adView.loadAd(AdRequest.Builder().build())
        (application as? MainApplication)?.showAdIfAvailable(this)
    }

    private fun hideAds() {
        binding.flNativeBanner.visibility = View.GONE
        binding.adView.visibility = View.GONE
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun purchaseSuccess(event: String) {
        if (event == PURCHASED) {
            Log.d("TAG", "purchaseSuccess: $event")
            hideAds()
            customDialog.showPremiumDialog(this)
        }
    }

    private fun checkShowBanner() {
        val checkPremium = PurchasePrefs(this).hasPremium
        if (!checkPremium) {
            remoteConfigBanner()
        }
    }

    private fun checkPremiumFlow() {
        val checkPremium = PurchasePrefs(this).hasPremium
        if (!checkPremium && !checkShowPurchase) {
            checkShowPurchase = true
            startActivity(Intent(this, PurchaseActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        binding.flNativeBanner.visibility = View.VISIBLE
        binding.adView.visibility = View.VISIBLE
        EventBus.getDefault().register(this)
        PresenceManager.setUserOnline(this)
        PresenceManager.listenOnlineUsers { onlineUsers ->
            val count = onlineUsers.size
            val menuItem = binding.drawerMain.menu.findItem(R.id.nav_menu_users)
            menuItem.title = getString(R.string.lb_user_online, count)
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        checkPremium()
    }

    override fun onDestroy() {
        super.onDestroy()
        PresenceManager.setUserOffline(this)
    }
}