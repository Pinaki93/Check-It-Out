package dev.pinaki.todoapp.ui.swipe

import androidx.recyclerview.widget.RecyclerView

interface OnSwipeCallback {
    fun onSwipeLeft(recyclerView: RecyclerView, position: Int)

    fun onSwipeRight(recyclerView: RecyclerView,position: Int)
}