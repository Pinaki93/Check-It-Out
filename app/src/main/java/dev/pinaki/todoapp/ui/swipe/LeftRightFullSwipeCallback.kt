package dev.pinaki.todoapp.ui.swipe

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class LeftRightFullSwipeCallback(
    context: Context,
    private val listener: OnSwipeCallback?,
    private val recyclerView: RecyclerView,
    @DrawableRes rightIcon: Int,
    @ColorInt rightBgColor: Int,
    @DrawableRes leftIcon: Int,
    @ColorInt leftBgColor: Int
) : ItemTouchHelper.SimpleCallback(
    0/*Signifies that we don't need to allow any dragging*/,
    ItemTouchHelper.LEFT/* or ItemTouchHelper.RIGHT*/ /*Currently only supporting left swipe for delete*/
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
        return false
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

            else -> {
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                rightBackground.setBounds(0, 0, 0, 0)
                leftBackground.setBounds(0, 0, 0, 0)

                rightBackground.draw(c)
                leftBackground.draw(c)
            }
        }

    }
}