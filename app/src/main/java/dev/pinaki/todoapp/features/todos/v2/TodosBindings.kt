package dev.pinaki.todoapp.features.todos.v2

import android.widget.EditText
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.common.util.showKeyboard
import dev.pinaki.todoapp.common.util.showStrikeThrough
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem

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

@BindingAdapter("app:forceShowKeyboard")
fun forceShowKeyboard(view: AddTodoItemView, showEvent: Event<Boolean>?) {
    if (showEvent==null || showEvent.hasBeenHandled) return

    showEvent.getContentIfNotHandled()?.let {
        if (it)
            view.showKeyboard()
    }
}