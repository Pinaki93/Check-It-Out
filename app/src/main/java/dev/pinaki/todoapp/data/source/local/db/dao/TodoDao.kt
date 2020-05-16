package dev.pinaki.todoapp.data.source.local.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem

@Dao
interface TodoDao {
    @Query("select * from todo_item order by item_order desc")
    suspend fun getAll(): List<TodoItem>

    @Query("select * from todo_item  where list_ref_id=:listId order by item_order desc")
    suspend fun getAllById(listId: Int): List<TodoItem>

    @Query("select * from todo_item where is_done=:isDone order by item_order desc")
    suspend fun getAllItems(isDone: Boolean): List<TodoItem>

    @Query("select * from todo_item  where list_ref_id=:listId order by item_order desc")
    fun getItemsByListId(listId: Int): LiveData<List<TodoItem>>

    @Transaction
    open suspend fun addItemAndUpdateOrder(item: TodoItem, updateOrderId: Boolean) {
        val insertId = add(item).toInt()

        if (updateOrderId) {
            val insertedItem = getItem(insertId)
            insertedItem.itemOrder = (insertId + 1).toDouble()
            update(insertedItem)
        }
    }

    @Insert
    suspend fun add(item: TodoItem): Long

    @Update
    suspend fun update(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)

    @Query("select * from todo_item where id=:id")
    suspend fun getItem(id: Int): TodoItem

    @Transaction
    @Query("delete from todo_item where list_ref_id=:listId")
    suspend fun deleteAll(listId: Int)

    @Transaction
    @Query("delete from todo_item where list_ref_id=:listId and is_done = 1")
    fun deleteCompleted(listId: Int)

    @Query("select * from todo_item where id=:id")
    suspend fun getTodo(id: Int): TodoItem
}