package dev.pinaki.todoapp.ui.features.todos.v2

import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.util.showKeyboard
import dev.pinaki.todoapp.util.showStrikeThrough

@BindingAdapter("app:todos")
fun setTodos(view: RecyclerView, list: List<TodoItem>?) {
    list?.run {
        (view.adapter as TodosAdapter).submitList(this)
    }

}

@BindingAdapter("app:showKeyboard")
fun showKeyboardForEdiText(view: EditText, show: Boolean) {
    if (show)
        view.context?.showKeyboard(view)
}

@BindingAdapter("app:onKeyboardDone")
fun setOnEditorAction(view: EditText, action: Runnable) {
    view.setOnEditorActionListener { _, _, _ ->
        action.run()
        return@setOnEditorActionListener true
    }
}

@BindingAdapter("app:strikeThrough")
fun setOnEditorAction(view: TextView, strikeThrough: Boolean) {
    view.showStrikeThrough(strikeThrough)
}