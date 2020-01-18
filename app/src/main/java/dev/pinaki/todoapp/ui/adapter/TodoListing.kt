package dev.pinaki.todoapp.ui.adapter

import android.app.Application
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.chauthai.swipereveallayout.ViewBinderHelper
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodoHeaderBinding
import dev.pinaki.todoapp.databinding.TodoItemBinding
import dev.pinaki.todoapp.ds.ComparableItem
import dev.pinaki.todoapp.ui.adapter.diffutil.TodoViewItemDiffUtilCallback
import dev.pinaki.todoapp.util.*
import java.util.*
import kotlin.collections.ArrayList

////////////////////////////////////////////////////////////////////////////////////////////
// View Items
////////////////////////////////////////////////////////////////////////////////////////////
private const val ITEM_TYPE_HEADER = 1
private const val ITEM_TYPE_CONTENT = 2

sealed class TodoViewItem : ComparableItem<TodoViewItem>

data class HeaderItem(val title: String, val count: Int) : TodoViewItem() {
    override fun isItemSame(other: TodoViewItem): Boolean {
        if (other !is HeaderItem) {
            return false
        }

        return title == other.title
    }

    override fun isContentSame(other: TodoViewItem): Boolean {
        if (other !is HeaderItem) {
            return false
        }

        return this == other
    }
}

data class ContentItem(val data: TodoItem) : TodoViewItem() {
    override fun isItemSame(other: TodoViewItem): Boolean {
        if (other !is ContentItem) {
            return false
        }

//        logd(
//            "DiffUtil",
//            "returning ${(data.id == other.data.id && data.done == other.data.done)} for isItemTheSame called for $this and $other"
//        )

        return data.id == other.data.id && data.done == other.data.done
    }

    override fun isContentSame(other: TodoViewItem): Boolean {
        if (other !is ContentItem) {
            return false
        }

        logd(
            "DiffUtil",
            "returning ${data.isContentSame(other = other.data)} isContentSame called for $this and $other"
        )

        return data.isContentSame(other = other.data)
    }
}

////////////////////////////////////////////////////////////////////////////////////////////
// Adapter
////////////////////////////////////////////////////////////////////////////////////////////
class TodoListingAdapter(val application: Application) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var onItemClick: ((TodoItem) -> Unit)? = null
    var onItemDelete: ((TodoItem) -> Unit)? = null

    var items: MutableList<TodoViewItem> = ArrayList()
        set(value) {
            val diff = DiffUtil.calculateDiff(TodoViewItemDiffUtilCallback(field, value))

            field.clear()
            field.addAll(value)

            diff.dispatchUpdatesTo(this)
        }

    private val bindHelper = ViewBinderHelper()

    init {
        bindHelper.setOpenOnlyOne(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        logd("DiffUtilTest", "onCreateViewHolder called, viewType:$viewType")
        return if (viewType == ITEM_TYPE_HEADER)
            HeaderViewHolder(getHeaderBinding(LayoutInflater.from(parent.context), parent))
        else
            ContentViewHolder(getContentBinding(LayoutInflater.from(parent.context), parent))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (
            getItemViewType(position) == ITEM_TYPE_HEADER
            && items[position] is HeaderItem && holder is HeaderViewHolder
        ) {
            holder.setItem(items[position] as HeaderItem)
        } else if (items[position] is ContentItem && holder is ContentViewHolder) {
            bindHelper.bind(
                holder.getSwipeRevealLayout(),
                (items[position] as ContentItem).data.id.toString()
            )
            holder.setItem((items[position] as ContentItem).data)

        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int, payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty()) {
            // no change
            super.onBindViewHolder(holder, position, payloads)
        } else {
            if (payloads[0] is Bundle) {
                val payload = payloads[0] as Bundle

                if (getItemViewType(position) == ITEM_TYPE_HEADER && holder is HeaderViewHolder) {
                    if (payload.containsKey(DIFF_UTIL_ARG_COUNT)) {
                        holder.setCount(payload.getInt(DIFF_UTIL_ARG_COUNT))
                    }

                    if (payload.containsKey(DIFF_UTIL_ARG_TITLE)) {
                        holder.setTitle(payload.getString(DIFF_UTIL_ARG_TITLE)!!)
                    }
                } else if (holder is ContentViewHolder) {
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
                        holder.hideDate()
                    }
                }
            }
        }
    }

    override fun getItemCount() = items.size

    override fun getItemViewType(position: Int) =
        if (items[position] is HeaderItem)
            ITEM_TYPE_HEADER
        else
            ITEM_TYPE_CONTENT

    fun moveItem(initialPosition: Int, finalPosition: Int) {
        Collections.swap(items, initialPosition, finalPosition)
        notifyItemMoved(initialPosition, finalPosition)
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Inner ViewHolder classes
    //////////////////////////////////////////////////////////////////////////////////////////////
    inner class HeaderViewHolder(private val binding: TodoHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setItem(todoViewItem: HeaderItem) {
            setTitle(todoViewItem.title)
            setCount(todoViewItem.count)
        }

        fun setCount(count: Int) {
            binding.tvCount.text = count.toString()
        }

        fun setTitle(title: String) {
            binding.tvHeader.text = title
        }
    }

    inner class ContentViewHolder(private val binding: TodoItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.todoItemContainer.setOnClickListener {
                if (items[adapterPosition] is ContentItem) {
                    onItemClick?.invoke((items[adapterPosition] as ContentItem).data.copy())
                    binding.swipeRevealLayoout.close(true)
                }
            }

            binding.cbTodoDone.setOnClickListener {
                if (items[adapterPosition] is ContentItem) {
                    onItemClick?.invoke((items[adapterPosition] as ContentItem).data.copy())
                    binding.swipeRevealLayoout.close(true)
                }
            }

            binding.btnDelete.setOnClickListener {
                onItemDelete?.invoke((items[adapterPosition] as ContentItem).data)
                binding.swipeRevealLayoout.close(true)
            }
        }

        fun setItem(todoItem: TodoItem) {
            binding.tvTitle.text = todoItem.title
            binding.cbTodoDone.isChecked = todoItem.done

            if (todoItem.done) {
                setDateCompleted(todoItem.dateCompeted!!)
            } else {
                hideDate()
            }
        }

        fun setTitle(title: String) {
            binding.tvTitle.text = title
        }

        fun setDone(done: Boolean) {
            binding.cbTodoDone.isChecked = done
        }

        fun setDateCompleted(dateCompeted: Date) {
            binding.tvDate.visibility = View.VISIBLE
            binding.tvDate.text = application.getString(
                R.string.lbl_item_completed_date,
                dateCompeted.getAsDisplayString()
            )

            binding.tvTitle.alpha = .40f
        }

        fun hideDate() {
            binding.tvDate.visibility = View.GONE
        }

        fun getSwipeRevealLayout() = binding.swipeRevealLayoout

        fun highlightItem(shouldHighlight: Boolean) {
            val elevation = if (shouldHighlight) 16 else 0
            ViewCompat.setElevation(binding.root, elevation.toDp(binding.root.context).toFloat())
            //TODO: Move these to colors.xml
            binding.root.setBackgroundColor(Color.parseColor(if (shouldHighlight) "#fcfcfc" else "#ffffff"))
        }

    }
}

//////////////////////////////////////////////////////////////////////////////////////////////
// Private Util Methods
//////////////////////////////////////////////////////////////////////////////////////////////
private fun getHeaderBinding(inflater: LayoutInflater, parent: ViewGroup?) =
    DataBindingUtil.inflate<TodoHeaderBinding>(
        inflater, R.layout.layout_todo_header, parent,
        false
    )

private fun getContentBinding(inflater: LayoutInflater, parent: ViewGroup?) =
    DataBindingUtil.inflate<TodoItemBinding>(
        inflater, R.layout.layout_todo_item, parent,
        false
    )