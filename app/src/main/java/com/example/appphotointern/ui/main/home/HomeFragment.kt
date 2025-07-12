package com.example.appphotointern.ui.main.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.appphotointern.R
import com.example.appphotointern.databinding.FragmentHomeBinding
import com.example.appphotointern.extention.toast
import com.example.appphotointern.models.Feature
import com.example.appphotointern.ui.main.HomeAdapter

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterHome: HomeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val demoList = listOf(
            Feature("Edit Photo", R.drawable.ic_photo, 1),
            Feature("Make a Collage", R.drawable.ic_collage, 2),
            Feature("Make a Collage", R.drawable.ic_backgrounds, 3),
            Feature("Make a Collage", R.drawable.ic_collage, 4),
        )

        adapterHome = HomeAdapter(
            demoList,
            onItemClick = { feature ->
                when (feature.featureType) {
                    1 -> {
                        requireContext().toast(R.string.toast_google_login_success)
                    }

                    2 -> {
                    }

                    3 -> {
                    }

                    4 -> {
                    }

                    else -> {
                    }
                }
            })

        binding.recFeature.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            adapter = adapterHome
        }
    }
}
