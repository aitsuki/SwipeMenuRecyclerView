package com.aitsuki.swipedemo.items

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.aitsuki.swipe.SwipeLayout
import com.aitsuki.swipedemo.BaseItem
import com.aitsuki.swipedemo.databinding.ItemClassicBinding

class ClassicItem : BaseItem<ItemClassicBinding>() {

    override fun inflate(inflater: LayoutInflater, parent: ViewGroup): ItemClassicBinding {
        return ItemClassicBinding.inflate(inflater, parent, false)
    }

    override val bindFun = fun (binding: ItemClassicBinding, position: Int) {
        binding.content.text = "Classic $position"
        binding.content.setOnClickListener {
            Toast.makeText(it.context, "Classic $position", Toast.LENGTH_SHORT).show()
        }

        binding.root.addListener(object : SwipeLayout.Listener {
            override fun onSwipeStateChanged(menuView: View, newState: Int) {
                if (newState == SwipeLayout.STATE_IDLE) {
                    binding.root.swipeFlags = SwipeLayout.LEFT or SwipeLayout.RIGHT
                }
            }
        })

        binding.content.setOnLongClickListener {
            binding.root.swipeFlags = 0
            binding.root.openRightMenu(true)
            true
        }
        binding.leftMenu.setOnClickListener {
            Toast.makeText(it.context, "LEFT $position", Toast.LENGTH_SHORT).show()
        }
        binding.rightMenu.setOnClickListener {
            Toast.makeText(it.context, "RIGHT $position", Toast.LENGTH_SHORT).show()
        }
    }

}