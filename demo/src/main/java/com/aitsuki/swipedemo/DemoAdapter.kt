package com.aitsuki.swipedemo

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.aitsuki.swipedemo.databinding.ItemOverlayBinding

class DemoAdapter : RecyclerView.Adapter<DemoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DemoViewHolder {
        return DemoViewHolder(
            ItemOverlayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: DemoViewHolder, position: Int) {
        holder.binding.content.text = "Item $position"
        holder.binding.content.setOnClickListener {
            Toast.makeText(it.context, "Item $position", Toast.LENGTH_SHORT).show()
        }
        holder.binding.leftMenu.setOnClickListener {
            holder.binding.root.closeMenu(false)
            Toast.makeText(it.context, "LEFT $position", Toast.LENGTH_SHORT).show()
        }
        holder.binding.rightMenu.setOnClickListener {
            holder.binding.root.closeMenu(false)
            Toast.makeText(it.context, "RIGHT $position", Toast.LENGTH_SHORT).show()
        }
    }

    override fun getItemCount(): Int {
        return 100
    }
}

class DemoViewHolder(val binding: ItemOverlayBinding) : RecyclerView.ViewHolder(binding.root)