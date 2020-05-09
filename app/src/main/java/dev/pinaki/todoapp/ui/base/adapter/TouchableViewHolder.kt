package dev.pinaki.todoapp.ui.base.adapter

import android.view.View

interface TouchableViewHolder {
    fun highlightItem(shouldHighlight: Boolean)

    fun getContentView(): View

    fun getDragFlags(): Int

    fun getSwipeFlags(): Int
}