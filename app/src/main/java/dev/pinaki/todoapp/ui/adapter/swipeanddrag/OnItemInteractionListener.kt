package dev.pinaki.todoapp.ui.adapter.swipeanddrag

import androidx.recyclerview.widget.RecyclerView

interface OnItemInteractionListener {
    fun onMove(recyclerView: RecyclerView, initialPosition: Int, finalPosition: Int)

    fun onItemSelected(recyclerView: RecyclerView, position: Int)

    fun onItemReleased(recyclerView: RecyclerView, position: Int)
}