package dev.pinaki.todoapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.pinaki.todoapp.data.db.entity.TodoItem

@Dao
interface TodoDao {
    @Query("select * from todo_item order by item_order desc")
    suspend fun getAll(): List<TodoItem>

    @Query("select * from todo_item where is_done=:isDone order by item_order desc")
    suspend fun getAllItems(isDone: Boolean): List<TodoItem>

    @Query("select * from todo_item  where list_ref_id=:listId order by item_order desc")
    fun getItemsByListId(listId: Int): LiveData<List<TodoItem>>

    @Insert
    suspend fun add(item: TodoItem): Long

    @Update
    suspend fun update(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)

    @Query("select * from todo_item where id=:id")
    suspend fun getItem(id: Int): TodoItem
}