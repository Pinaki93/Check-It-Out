package dev.pinaki.todoapp.ui.adapter.swipeanddrag

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.ui.adapter.TodoListingAdapter

class TodoItemRecyclerViewCallback(
    private val listener: OnItemInteractionListener?,
    private val recyclerView: RecyclerView
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    0
) {
    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        listener?.onMove(recyclerView, viewHolder.adapterPosition, target.adapterPosition)

        return true
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder
    ) =
        if (viewHolder is TodoListingAdapter.ContentViewHolder)
            makeMovementFlags(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                0
            )
        else {
            makeMovementFlags(0, 0)
        }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            if (viewHolder is TodoListingAdapter.ContentViewHolder) {
                viewHolder.highlightItem(true)
            }

            listener?.onItemSelected(recyclerView, viewHolder.adapterPosition)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is TodoListingAdapter.ContentViewHolder) {
            viewHolder.highlightItem(false)
        }

        listener?.onItemReleased(recyclerView, viewHolder.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        //stub
    }
}