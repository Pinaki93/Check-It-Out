package dev.pinaki.todoapp.common.ui.adapter

import androidx.recyclerview.widget.RecyclerView

interface OnItemInteractionListener {
    fun onMove(recyclerView: RecyclerView, initialPosition: Int, finalPosition: Int)

    fun onItemSelected(recyclerView: RecyclerView, position: Int)

    fun onItemReleased(recyclerView: RecyclerView, position: Int)

    fun onSwipeLeft(recyclerView: RecyclerView, position: Int)

    fun onSwipeRight(recyclerView: RecyclerView, position: Int)
}