package com.aitsuki.swipedemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

class DemoAdapter<T : ViewBinding>(private val item: BaseItem<T>) :
    RecyclerView.Adapter<SimpleViewHolder<T>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<T> {
        return SimpleViewHolder(item.onCreate(parent), item.bindFun)
    }

    override fun onBindViewHolder(holder: SimpleViewHolder<T>, position: Int) {
        holder.bindFun(holder.binding, position)
    }

    override fun getItemCount(): Int {
        return 100
    }
}

class SimpleViewHolder<T : ViewBinding>(
    val binding: T,
    val bindFun: (binding: T, position: Int) -> Unit
) : RecyclerView.ViewHolder(binding.root)

abstract class BaseItem<T : ViewBinding> {

    fun onCreate(parent: ViewGroup): T {
        return inflate(LayoutInflater.from(parent.context), parent)
    }

    abstract fun inflate(inflater: LayoutInflater, parent: ViewGroup): T

    abstract val bindFun: (T, Int) -> Unit
}
