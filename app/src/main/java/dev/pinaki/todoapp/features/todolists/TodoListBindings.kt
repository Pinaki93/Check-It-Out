package dev.pinaki.todoapp.features.todolists

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.data.source.local.db.entity.TodoList

@BindingAdapter("app:todoList")
fun setTodoList(recyclerView: RecyclerView, list: List<TodoList>?) {
    list?.run {
        ((recyclerView.adapter) as TodoListAdapter).submitList(list)
    }
}