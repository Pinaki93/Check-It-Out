package dev.pinaki.todoapp.bus

import androidx.lifecycle.MutableLiveData

object TodoBus : MutableLiveData<TodoBusItem>()

data class TodoBusItem(val eventId: Int) {
    companion object {
        const val EVENT_TODO_ITEM_ADDED = 1
    }
}