package dev.pinaki.todoapp.data.db.dao

import androidx.room.*
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.data.db.entity.TodoListWithItems

@Dao
interface TodoListDao {

    @Query("select * from todo_list")
    suspend fun getAll(): List<TodoList>

    @Transaction
    @Query("select * from todo_list where id=:id")
    suspend fun getTodosForList(id: String): List<TodoListWithItems>

    @Insert
    suspend fun addTodoList(list: TodoList)

    @Insert
    suspend fun updateTodoList(list: TodoList)

    @Delete
    suspend fun deleteTodoList(list: TodoList)
}