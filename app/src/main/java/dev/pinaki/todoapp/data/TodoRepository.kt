package dev.pinaki.todoapp.data

import android.content.Context
import dev.pinaki.todoapp.data.db.TodoDatabase
import dev.pinaki.todoapp.data.db.entity.TodoItem
import java.util.*

class TodoRepository(context: Context) {

    private val db = TodoDatabase.getInstance(context)
    private val todoDao = db.todoDao()

    suspend fun getAll() = todoDao.getAll()

    suspend fun addTodo(item: TodoItem) {
        val insertId = todoDao.add(item).toInt()

        val insertedItem = todoDao.getItem(insertId)
        insertedItem.itemOrder = (insertId + 1).toDouble()
        todoDao.update(insertedItem)
    }

    suspend fun updateTodo(item: TodoItem) {
        item.dateModified = Date()
        todoDao.update(item)
    }

    suspend fun deleteTodo(item: TodoItem) {
        todoDao.delete(item)
    }

    suspend fun moveItem(
        itemToMove: TodoItem,
        start: Int,
        end: Int
    ) {
        // get all the items in descending orders of item_order
        val allItems = todoDao.getAllItems(itemToMove.done)

        val size = allItems.size

        // we will take item_order of the items before and after the item to be moved
        // and then assign the average of their item_order to our current item

        // 1. before index: the item_order of the item before the item to be moved
        // by default we will consider this to be 1 greater
        // than the highest `item_order` item
        // [i.e: the highest item_order within the list]
        var beforeIndex = allItems[0].itemOrder + 1

        // 2. after index: the item_order of the item after the item to be moved
        // by default we will take it to be zero,
        // as every item order will be >0
        var afterIndex = 0.0

        if (end == 0) {
            // worst case scenario 1
            // item being placed at the top
            // we already have before index as 1 greater than the highest
            // so after index will be the highest item_order in the list
            afterIndex = allItems[end].itemOrder
        } else if (end == size - 1) {
            // worst case scenario 2
            // item being placed at the bottom
            // we already have after index as 0
            // so before index will be the lowest item_order in the list (>0)
            beforeIndex = allItems[end].itemOrder
        } else {
            // middle scenarios
            if (start > end) {
                // item being dragged up
                // so eg we have 4 items 1,2,3,4 and 1 is being between 3 and 4,
                // it's position will move from index 0 to 2 and we will have
                // to give it a item_order between items[2] and items[3]
                beforeIndex = allItems[end - 1].itemOrder
                afterIndex = allItems[end].itemOrder
            } else {
                // item being dragged down
                // so eg we have 4 items 1,2,3,4 and 4 is being between 1 and 2,
                // it's position will move from index 3 to 1 and we will have
                // to give it a item_order between items[0] and items[1]
                beforeIndex = allItems[end].itemOrder
                afterIndex = allItems[end + 1].itemOrder
            }
        }

        // finally average out the before and after index
        // so that we can give an average weight between the two items
        val newItemIndex = (beforeIndex + afterIndex) / 2
        itemToMove.itemOrder = newItemIndex
        updateTodo(itemToMove)
    }
}