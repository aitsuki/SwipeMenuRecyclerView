package com.aitsuki.swipedemo.items

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.aitsuki.swipedemo.BaseItem
import com.aitsuki.swipedemo.databinding.ItemLongBinding

class LongItem : BaseItem<ItemLongBinding>() {

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemLongBinding {
        return ItemLongBinding.inflate(inflater, parent, false)
    }

    override val bindFun = fun (binding: ItemLongBinding, position: Int) {
        binding.content.text = "Long $position"
        binding.content.setOnClickListener {
            Toast.makeText(it.context, "Long $position", Toast.LENGTH_SHORT).show()
        }
        binding.leftMenu.setOnClickListener {
            Toast.makeText(it.context, "LEFT $position", Toast.LENGTH_SHORT).show()
        }
        binding.rightMenu.setOnClickListener {
            Toast.makeText(it.context, "RIGHT $position", Toast.LENGTH_SHORT).show()
        }
    }
}