package dev.pinaki.todoapp.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.data.db.entity.TodoListWithItems

@Dao
interface TodoListDao {

    @Query("select * from todo_list")
    suspend fun getAll(): List<TodoList>

    @Transaction
    @Query("select * from todo_list where id=:id")
    fun observeTodosForList(id: Int): LiveData<TodoListWithItems>

    @Insert
    suspend fun addTodoList(list: TodoList)

    @Insert
    suspend fun updateTodoList(list: TodoList)

    @Delete
    suspend fun deleteTodoList(list: TodoList)

    @Query("select * from todo_list")
    fun observeAllTodoList(): LiveData<List<TodoList>>

    @Query("select * from todo_list where id=:id")
    fun observeTodoListById(id: Int): LiveData<TodoList>
}