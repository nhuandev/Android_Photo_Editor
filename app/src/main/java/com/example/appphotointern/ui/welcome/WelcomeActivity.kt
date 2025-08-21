package com.example.appphotointern.ui.welcome

import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.viewpager2.widget.ViewPager2
import com.example.appphotointern.R
import com.example.appphotointern.repository.impl.WelcomeRepository
import com.example.appphotointern.databinding.ActivityWelcomeBinding
import com.example.appphotointern.ui.main.MainActivity
import com.example.appphotointern.common.BaseActivity
import kotlin.getValue

class WelcomeActivity : BaseActivity() {
    private val binding by lazy { ActivityWelcomeBinding.inflate(layoutInflater) }
    private val viewModelWelcome: WelcomeViewModel by lazy { WelcomeViewModel(WelcomeRepository(this)) }

    private lateinit var pagerAdapter: WelcomePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        pagerAdapter = WelcomePagerAdapter(this)
        initUI()
    }

    private fun initUI() {
        binding.apply {
            pagerAdapter.addFragment(
                WelcomeFragment.newInstance(
                    getString(R.string.lb_title_1),
                    getString(R.string.lb_desc_1),
                    R.drawable.img_welcome_2
                )
            )
            pagerAdapter.addFragment(
                WelcomeFragment.newInstance(
                    getString(R.string.lb_title_2),
                    getString(R.string.lb_desc_2),
                    R.drawable.img_welcome_1
                )
            )
            pagerAdapter.addFragment(
                WelcomeFragment.newInstance(
                    getString(R.string.lb_title_3),
                    getString(R.string.lb_desc_3),
                    R.drawable.img_welcome_3
                )
            )
            welcomeViewPager.adapter = pagerAdapter
            welcomeViewPager.isUserInputEnabled = false
            welcomeViewPager.registerOnPageChangeCallback(object :
                ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position == pagerAdapter.itemCount - 1) {
                        btnStarted.visibility = View.VISIBLE
                        nextButton.visibility = View.GONE
                        btnPrev.visibility = View.GONE
                    } else {
                        btnStarted.visibility = View.GONE
                    }
                }
            })

            nextButton.setOnClickListener {
                if (welcomeViewPager.currentItem < pagerAdapter.itemCount - 1) {
                    welcomeViewPager.currentItem = welcomeViewPager.currentItem + 1
                } else {
                    viewModelWelcome.markWelcome()
                    startMainActivity()
                }
            }

            btnPrev.setOnClickListener {
                welcomeViewPager.currentItem = welcomeViewPager.currentItem - 1
            }

            btnStarted.setOnClickListener {
                viewModelWelcome.markWelcome()
                startMainActivity()
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this@WelcomeActivity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
