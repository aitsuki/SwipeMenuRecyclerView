package com.aitsuki.swipedemo.items

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.aitsuki.swipedemo.BaseItem
import com.aitsuki.swipedemo.databinding.ItemParallaxBinding

class ParallaxItem : BaseItem<ItemParallaxBinding>() {

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemParallaxBinding {
        return ItemParallaxBinding.inflate(inflater, parent, false)
    }

    override val bindFun = fun (binding :ItemParallaxBinding, position: Int) {
        binding.content.text = "Parallax $position"
        binding.content.setOnClickListener {
            Toast.makeText(it.context, "Parallax $position", Toast.LENGTH_SHORT).show()
        }
        binding.left1.setOnClickListener {
            Toast.makeText(it.context, "Parallax L1 $position", Toast.LENGTH_SHORT).show()
        }
        binding.left2.setOnClickListener {
            Toast.makeText(it.context, "Parallax L2 $position", Toast.LENGTH_SHORT).show()
        }
        binding.left3.setOnClickListener {
            Toast.makeText(it.context, "Parallax L3 $position", Toast.LENGTH_SHORT).show()
        }
        binding.right1.setOnClickListener {
            Toast.makeText(it.context, "Parallax R1 $position", Toast.LENGTH_SHORT).show()
        }
        binding.right2.setOnClickListener {
            Toast.makeText(it.context, "Parallax R2 $position", Toast.LENGTH_SHORT).show()
        }
        binding.right3.setOnClickListener {
            Toast.makeText(it.context, "Parallax R3 $position", Toast.LENGTH_SHORT).show()
        }
    }
}