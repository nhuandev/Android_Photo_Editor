package com.example.appphotointern

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.appphotointern.databinding.ActivityMainBinding
import com.example.appphotointern.extention.isHasPermission
import com.example.appphotointern.extention.toast
import com.example.appphotointern.ui.main.ViewPagerAdapter
import com.example.appphotointern.ui.main.add.AddFragment
import com.example.appphotointern.ui.main.home.HomeFragment
import com.example.appphotointern.ui.main.setting.SettingFragment

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private var mPermission = Manifest.permission.READ_EXTERNAL_STORAGE

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initPermissionLauncher()
        initUI()
    }

    private fun initUI() {
        val fragments = listOf(
            HomeFragment(),
            AddFragment(),
            SettingFragment()
        )

        val adapterMain = ViewPagerAdapter(this, fragments)

        binding.apply {
            viewpagerMain.adapter = adapterMain
            viewpagerMain.isUserInputEnabled = false

            viewpagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                @SuppressLint("UseKtx")
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    bottomNavigation.menu.getItem(position).isChecked = true

                    when (position) {
                        0 -> toolBar.visibility = View.VISIBLE
                        1 -> toolBar.visibility = View.GONE
                        2 -> toolBar.visibility = View.GONE
                    }
                }
            })

            bottomNavigation.setOnItemSelectedListener {
                when (it.itemId) {
                    R.id.nav_home -> viewpagerMain.currentItem = 0
                    R.id.nav_add -> viewpagerMain.currentItem = 1
                    R.id.nav_setting -> viewpagerMain.currentItem = 2
                }
                true
            }

            toolBar.setOnClickListener {
                if (isHasReadPermission()) {
                    toast(R.string.lb_permission_denied)
                } else {
                    requestReadPermission()
                }
            }
        }
    }

    private fun initPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(RequestPermission()) { isGranted ->
            when {
                isGranted -> startActivity(Intent(this, MainActivity::class.java))

                shouldShowRequestPermissionRationale(mPermission) -> {
                    // show dialog description permission
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
                    // show dialog go to setting
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
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
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
