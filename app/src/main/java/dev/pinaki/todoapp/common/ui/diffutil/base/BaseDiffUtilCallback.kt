package dev.pinaki.todoapp.common.ui.diffutil.base

import androidx.recyclerview.widget.DiffUtil
import dev.pinaki.todoapp.data.ds.ComparableItem

class BaseDiffUtilCallback<T : ComparableItem<T>> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) = oldItem.isItemSame(newItem)

    override fun areContentsTheSame(oldItem: T, newItem: T) = oldItem.isContentSame(newItem)
}