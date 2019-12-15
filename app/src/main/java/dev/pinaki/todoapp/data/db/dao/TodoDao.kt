package dev.pinaki.todoapp.data.db.dao

import androidx.room.*
import dev.pinaki.todoapp.data.db.entity.TodoItem

@Dao
interface TodoDao {
    @Query("select * from todo_item")
    suspend fun getAll(): List<TodoItem>

    @Insert
    suspend fun add(item: TodoItem)

    @Update
    suspend fun update(item: TodoItem)

    @Delete
    suspend fun delete(item: TodoItem)
}