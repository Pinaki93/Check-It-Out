package dev.pinaki.todoapp.ui.features.todolists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.databinding.TodoListListItemBinding
import dev.pinaki.todoapp.ui.base.adapter.BaseAdapter

class TodoListAdapter(private val viewModel: AllListsViewModel) :
    BaseAdapter<TodoList, TodoListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setItem(getItem(position), viewModel)
    }

    class ViewHolder(private val binding: TodoListListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: TodoList, viewModel: AllListsViewModel) {
            binding.todoList = item
            binding.viewModel = viewModel
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val binding = DataBindingUtil.inflate<TodoListListItemBinding>(
                    LayoutInflater.from(parent.context),
                    R.layout.layout_todo_list_list_item,
                    parent,
                    false
                )

                return ViewHolder(binding)
            }
        }
    }
}