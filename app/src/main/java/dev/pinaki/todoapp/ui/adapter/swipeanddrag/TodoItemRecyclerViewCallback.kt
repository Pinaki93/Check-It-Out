package dev.pinaki.todoapp.ui.adapter.swipeanddrag

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.ColorDrawable
import android.util.Log
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.ui.adapter.TodoListingAdapter

class TodoItemRecyclerViewCallback(
    context: Context,
    private val listener: OnItemInteractionListener?,
    private val recyclerView: RecyclerView,
    @DrawableRes rightIcon: Int,
    @ColorInt rightBgColor: Int,
    @DrawableRes leftIcon: Int,
    @ColorInt leftBgColor: Int
) : ItemTouchHelper.SimpleCallback(
    ItemTouchHelper.UP or ItemTouchHelper.DOWN,
    0
) {
    private val rightIcon = ContextCompat.getDrawable(context, rightIcon)!!
    private val rightBackground = ColorDrawable(rightBgColor)

    private val leftIcon = ContextCompat.getDrawable(context, leftIcon)!!
    private val leftBackground = ColorDrawable(leftBgColor)

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
        val itemView = viewHolder.itemView

        val iconMargin = (itemView.height - rightIcon.intrinsicHeight) / 2
        val iconTop =
            itemView.top + (itemView.height - rightIcon.intrinsicHeight) / 2
        val iconBottom = iconTop + rightIcon.intrinsicHeight

        when {
            !isCurrentlyActive || dX == 0f -> {
                ItemTouchHelper.Callback.getDefaultUIUtil()
                rightBackground.setBounds(0, 0, 0, 0)
                leftBackground.setBounds(0, 0, 0, 0)

                rightBackground.draw(c)
                leftBackground.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            dX > 0 -> {
                // right swipe
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )

                val iconLeft = itemView.left + iconMargin
                val iconRight = itemView.left + iconMargin + rightIcon.intrinsicWidth
                leftIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                leftBackground.setBounds(
                    itemView.left + dX.toInt(),
                    itemView.top, itemView.left, itemView.bottom
                )

                leftBackground.draw(c)
                leftIcon.draw(c)
            }

            dX < 0 -> {
                // left swipe
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                val iconLeft = itemView.right - iconMargin - rightIcon.intrinsicWidth
                val iconRight = itemView.right - iconMargin
                rightIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                rightBackground.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top, itemView.right, itemView.bottom
                )

                rightBackground.draw(c)
                rightIcon.draw(c)
            }
        }
    }
}