package dev.pinaki.todoapp.ui.adapter.diffutil

import dev.pinaki.todoapp.ui.adapter.TodoViewItem
import dev.pinaki.todoapp.ui.adapter.diffutil.base.BaseDiffUtilCallback
import dev.pinaki.todoapp.util.getChangePayload

class TodoViewItemDiffUtilCallback(
    private val oldList: List<TodoViewItem>,
    private val newList: List<TodoViewItem>
) :
    BaseDiffUtilCallback<TodoViewItem>(oldList, newList) {

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return oldList[oldItemPosition].getChangePayload(newList[newItemPosition])
    }
}