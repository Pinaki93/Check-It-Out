package dev.pinaki.todoapp.ui.features.todos.v2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodosBinding
import dev.pinaki.todoapp.ui.base.adapter.BaseAdapter

class TodosAdapter(private val viewModel: TodoListViewModel) :
    BaseAdapter<TodoItem, TodosAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder.from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.setItem(getItem(position))
    }


    class TodoViewHolder(
        private val viewModel: TodoListViewModel,
        private val binding: TodosBinding
    ) :
        RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: TodoItem) {
            binding.item = item
            binding.viewmodel = viewModel
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: TodoListViewModel): TodoViewHolder {
                return TodoViewHolder(
                    viewModel = viewModel,
                    binding = DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.layout_todo_item_new,
                        parent,
                        false
                    )
                )
            }
        }
    }
}