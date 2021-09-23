package com.aitsuki.swipedemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.viewbinding.ViewBinding
import com.aitsuki.swipedemo.databinding.FragmentDemoBinding
import com.aitsuki.swipedemo.items.*
import com.google.android.material.bottomsheet.BottomSheetBehavior

class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null

    private val binding get() = _binding!!
    private val styleBinding get() = binding.fragmentStyleBinding

    lateinit var behavior: BottomSheetBehavior<*>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater, container, false)
        binding.recycleView
            .addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        behavior = BottomSheetBehavior.from(binding.bottomSheet)
        behavior.isHideable = true
        behavior.peekHeight = 0
        binding.floatingButton.setOnClickListener {
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
        }

        styleBinding.itemClassic.content.setOnClickListener {
            changeStyle(ClassicItem())
        }

        styleBinding.itemOverlay.content.setOnClickListener {
            changeStyle(OverlayItem())
        }

        styleBinding.itemLong.content.setOnClickListener {
            changeStyle(LongItem())
        }

        styleBinding.itemTencentQQ.content.setOnClickListener {
            changeStyle(QQItem())
        }

        changeStyle(ClassicItem())
        return binding.root
    }

    private fun <T: ViewBinding> changeStyle(baseItem: BaseItem<T>) {
        binding.recycleView.adapter = DemoAdapter(baseItem)
        behavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}