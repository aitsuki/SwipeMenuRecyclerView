package com.aitsuki.swipedemo.items

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.aitsuki.swipedemo.BaseItem
import com.aitsuki.swipedemo.databinding.ItemTencentQQBinding

class QQItem : BaseItem<ItemTencentQQBinding>() {

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemTencentQQBinding {
        return ItemTencentQQBinding.inflate(inflater, parent, false)
    }

    override val bindFun = fun (binding: ItemTencentQQBinding, position: Int) {
        val root = binding.root
        for (i in 0 until root.childCount) {
            root.getChildAt(i).setOnClickListener {
                val text = if (it is TextView) {
                    "${it.text} $position"
                } else {
                    "Click child[$i] $position"
                }
                Toast.makeText(it.context, text, Toast.LENGTH_SHORT).show()
            }
        }


        binding.content.text = "Tencent QQ $position"
        binding.content.setOnClickListener {
            Toast.makeText(it.context, "Tencent QQ $position", Toast.LENGTH_SHORT).show()
        }
        binding.delete.setOnClickListener {
            Toast.makeText(it.context, "Delete $position", Toast.LENGTH_SHORT).show()
        }
        binding.markAsRead.setOnClickListener {
            Toast.makeText(it.context, "Read $position", Toast.LENGTH_SHORT).show()
        }
        binding.top.setOnClickListener {
            Toast.makeText(it.context, "Top $position", Toast.LENGTH_SHORT).show()
        }
    }
}