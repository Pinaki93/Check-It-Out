package dev.pinaki.todoapp.features.todos.v2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.adapter.BaseAdapter
import dev.pinaki.todoapp.common.ui.adapter.TouchableViewHolder
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodosBinding
import java.util.*
import kotlin.collections.ArrayList

class TodosAdapter(private val viewModel: TodosViewModel) :
    BaseAdapter<TodoItem, TodosAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder.from(parent, viewModel)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.setItem(getItem(position))
    }

    fun moveItem(initialPosition: Int, finalPosition: Int) {
        val currentList = ArrayList(currentList)
        Collections.swap(currentList, initialPosition, finalPosition)
        submitList(currentList)
    }

    class TodoViewHolder(
        private val viewModel: TodosViewModel,
        private val binding: TodosBinding
    ) :
        RecyclerView.ViewHolder(binding.root), TouchableViewHolder {

        fun setItem(item: TodoItem) {
            binding.item = item
            binding.viewmodel = viewModel
        }

        override fun highlightItem(shouldHighlight: Boolean) {
            //TODO
        }

        override fun getContentView() = binding.todoItemContainerParent

        override fun getDragFlags(): Int {
            return ItemTouchHelper.UP or ItemTouchHelper.DOWN

        }

        override fun getSwipeFlags(): Int {
            return ItemTouchHelper.LEFT
        }

        companion object {
            fun from(parent: ViewGroup, viewModel: TodosViewModel): TodoViewHolder {
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