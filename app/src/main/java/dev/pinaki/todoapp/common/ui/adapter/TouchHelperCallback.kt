package dev.pinaki.todoapp.common.ui.adapter

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class TouchHelperCallback(
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
        if (viewHolder is TouchableViewHolder)
            makeMovementFlags(
                viewHolder.getDragFlags(),
                viewHolder.getSwipeFlags()
            )
        else {
            makeMovementFlags(0, 0)
        }


    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        viewHolder?.let {
            if (viewHolder is TouchableViewHolder) {
                ItemTouchHelper.Callback.getDefaultUIUtil().onSelected(viewHolder.getContentView())

                viewHolder.highlightItem(true)
            }

            listener?.onItemSelected(recyclerView, viewHolder.adapterPosition)
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is TouchableViewHolder) {
            ItemTouchHelper.Callback.getDefaultUIUtil().clearView(viewHolder.getContentView())

            viewHolder.highlightItem(false)
        }

        listener?.onItemReleased(recyclerView, viewHolder.adapterPosition)
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition

        when (direction) {
            ItemTouchHelper.LEFT -> {
                listener?.onSwipeLeft(recyclerView, position)
            }

            ItemTouchHelper.RIGHT -> {
                listener?.onSwipeRight(recyclerView, position)
            }
        }
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        if (dY == 0f && viewHolder is TouchableViewHolder) {
            if (dX <= 0) {
                // swipe left
                ItemTouchHelper.Callback.getDefaultUIUtil().onDraw(
                    c, recyclerView, viewHolder.getContentView(),
                    dX, dY, actionState, isCurrentlyActive
                )
            }
        } else {
            super.onChildDraw(
                c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
            )
        }
    }
}