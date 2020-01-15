package dev.pinaki.todoapp.ui.adapter.diffutil

import dev.pinaki.todoapp.ui.adapter.TodoViewItem
import dev.pinaki.todoapp.ui.adapter.diffutil.base.BaseDiffUtilCallback
import dev.pinaki.todoapp.util.getChangePayload
import dev.pinaki.todoapp.util.logd

class TodoViewItemDiffUtilCallback(
    private val oldList: List<TodoViewItem>,
    private val newList: List<TodoViewItem>
) :
    BaseDiffUtilCallback<TodoViewItem>(oldList, newList) {

    init {
        oldList.forEachIndexed { index, todoViewItem ->
            logd("DiffUtilList", "position: $index, item: $todoViewItem")
        }

        newList.forEachIndexed { index, todoViewItem ->
            logd("DiffUtilList", "position: $index, item: $todoViewItem")
        }
    }

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return oldList[oldItemPosition].getChangePayload(newList[newItemPosition])
    }
}