package dev.pinaki.todoapp.ui.adapter.diffutil

import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.ui.adapter.diffutil.base.BaseDiffUtilCallback
import dev.pinaki.todoapp.util.getChangePayload

class TodoItemDiffUtilCallback(
    private val oldList: List<TodoItem>,
    private val newList: List<TodoItem>
) :
    BaseDiffUtilCallback<TodoItem>(oldList, newList) {

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
        return oldList[oldItemPosition].getChangePayload(newList[newItemPosition])
    }
}