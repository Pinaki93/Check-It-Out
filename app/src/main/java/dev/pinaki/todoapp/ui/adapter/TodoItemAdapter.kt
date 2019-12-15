package dev.pinaki.todoapp.ui.adapter

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodoItemBinding
import dev.pinaki.todoapp.ui.adapter.diffutil.TodoItemDiffUtilCallback
import dev.pinaki.todoapp.util.*
import java.util.*
import kotlin.collections.ArrayList

class TodoItemAdapter(
    private val application: Application,
    private val items: MutableList<TodoItem>
) :
    RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder>() {

    var onItemClick: ((TodoItem) -> Unit)? = null

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Callbacks from parent fragment
    //////////////////////////////////////////////////////////////////////////////////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        return TodoItemViewHolder(
            getTodoItemBinding(
                parent
            )
        )
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        holder.setItem(items[position])
    }

    override fun onBindViewHolder(
        holder: TodoItemViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            val payload = payloads[0] as Bundle
            if (payload.isNotEmpty()) {
                if (payload.containsKey(DIFF_UTIL_ARG_TITLE)) {
                    holder.setTitle(payload.getString(DIFF_UTIL_ARG_TITLE)!!)
                }

                if (payload.containsKey(DIFF_UTIL_ARG_DONE)) {
                    holder.setDone(payload.getBoolean(DIFF_UTIL_ARG_DONE, false))
                }

                if (payload.containsKey(DIFF_UTIL_ARG_DATE_COMPLETED)) {
                    holder.setDateCompleted(Date(payload.getLong(DIFF_UTIL_ARG_DATE_COMPLETED)))
                }

                if (payload.containsKey(DIFF_UTIL_ARG_DATE_MODIFIED)) {
                    holder.setDateModified(Date(payload.getLong(DIFF_UTIL_ARG_DATE_COMPLETED)))
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun getItemCount() = items.count()

    //////////////////////////////////////////////////////////////////////////////////////////////
    // exposed Utility methods
    //////////////////////////////////////////////////////////////////////////////////////////////
    fun updateItems(items: List<TodoItem>) {
        val diff = DiffUtil.calculateDiff(TodoItemDiffUtilCallback(this.items, items))

        this.items.clear()
        this.items.addAll(items)

        diff.dispatchUpdatesTo(this)
    }

    fun getItemAtPosition(position: Int) = items[position]

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Inner ViewHolder classes
    //////////////////////////////////////////////////////////////////////////////////////////////
    inner class TodoItemViewHolder(private val binding: TodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }

            binding.cbTodoDone.setOnClickListener {
                onItemClick?.invoke(items[adapterPosition])
            }

            itemView.setOnLongClickListener {
                return@setOnLongClickListener true
            }
        }

        fun setItem(todoItem: TodoItem) {
            binding.tvTitle.text = todoItem.title
            binding.cbTodoDone.isChecked = todoItem.done

            if (todoItem.done) {
                binding.tvDate.text = application.getString(
                    R.string.lbl_item_completed_date,
                    todoItem.dateCompeted!!.getAsDisplayString()
                )
                binding.tvTitle.alpha = .40f
            } else {
                binding.tvDate.text = application.getString(
                    R.string.lbl_last_modified,
                    todoItem.dateModified.getAsDisplayString()
                )
                binding.tvTitle.alpha = 1f
            }
        }

        fun setTitle(title: String) {
            binding.tvTitle.text = title
        }

        fun setDone(done: Boolean) {
            binding.cbTodoDone.isChecked = done
        }

        fun setDateCompleted(dateCompeted: Date) {
            binding.tvDate.text = application.getString(
                R.string.lbl_item_completed_date,
                dateCompeted.getAsDisplayString()
            )

            binding.tvTitle.alpha = .40f
        }

        fun setDateModified(dateModified: Date) {
            binding.tvDate.text = application.getString(
                R.string.lbl_last_modified,
                dateModified.getAsDisplayString()
            )

            binding.tvTitle.alpha = 1f
        }

    }
}