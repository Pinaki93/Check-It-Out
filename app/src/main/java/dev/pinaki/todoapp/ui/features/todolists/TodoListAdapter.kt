package dev.pinaki.todoapp.ui.features.todolists

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.databinding.TodoListListItemBinding
import dev.pinaki.todoapp.ui.base.diffutil.base.BaseDiffUtilCallback

class TodoListAdapter : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    var items: List<TodoList> = ArrayList()
        set(value) {
            val ourList = field as ArrayList

            val diff = BaseDiffUtilCallback(ourList, value)
            ourList.clear()
            ourList.addAll(value)
            DiffUtil.calculateDiff(diff).dispatchUpdatesTo(this)
        }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        val binding = DataBindingUtil.inflate<TodoListListItemBinding>(
            LayoutInflater.from(parent.context),
            R.layout.layout_todo_list_list_item, parent, false
        )

        return TodoListViewHolder(binding)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        holder.setItem(items[position])
    }

    inner class TodoListViewHolder(private val binding: TodoListListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setItem(item: TodoList) {
            binding.tvTitle.text = item.title
        }
    }
}