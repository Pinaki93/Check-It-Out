package dev.pinaki.todoapp.ui.adapter.diffutil.base

import androidx.recyclerview.widget.DiffUtil
import dev.pinaki.todoapp.ds.ComparableItem

abstract class BaseDiffUtilCallback<T : ComparableItem<T>>(
    private val oldList: List<T>,
    private val newList: List<T>
) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].isItemSame(newList[newItemPosition])

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
        oldList[oldItemPosition].isContentSame(newList[newItemPosition])
}