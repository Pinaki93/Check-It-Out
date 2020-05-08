package dev.pinaki.todoapp.ui.features.todos.v2

import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.ui.view.AddTodoItemView

@BindingAdapter("app:todos")
fun setTodos(view: RecyclerView, list: List<TodoItem>?) {
    list?.run {
        (view.adapter as TodosAdapter).submitList(this)
    }

}

@BindingAdapter("app:collapse")
fun expandAddTodoView(view: AddTodoItemView, collapse: Boolean) {
    view.showCollapsedView(collapse)
}