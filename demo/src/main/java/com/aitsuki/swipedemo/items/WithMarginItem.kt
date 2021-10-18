package com.aitsuki.swipedemo.items

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.aitsuki.swipedemo.BaseItem
import com.aitsuki.swipedemo.databinding.ItemWithMarginBinding

class WithMarginItem : BaseItem<ItemWithMarginBinding>() {

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemWithMarginBinding {
        return ItemWithMarginBinding.inflate(inflater, parent, false)
    }

    override val bindFun = fun (binding: ItemWithMarginBinding, position: Int) {
        binding.content.text = "With margin $position"
        binding.content.setOnClickListener {
            Toast.makeText(it.context, "With margin $position", Toast.LENGTH_SHORT).show()
        }
        binding.leftMenu.setOnClickListener {
            Toast.makeText(it.context, "LEFT $position", Toast.LENGTH_SHORT).show()
        }
        binding.rightMenu.setOnClickListener {
            Toast.makeText(it.context, "RIGHT $position", Toast.LENGTH_SHORT).show()
        }
    }

}