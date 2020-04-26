package dev.pinaki.todoapp.data.db.dao

import androidx.room.Dao
import androidx.room.Query

@Dao
interface TodoListDao {

    @Query("select * from todo_list")
    suspend fun getAll(): List<TodoListDao>
}