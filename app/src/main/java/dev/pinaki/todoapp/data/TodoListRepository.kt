package dev.pinaki.todoapp.data

import android.content.Context
import dev.pinaki.todoapp.data.db.TodoDatabase
import dev.pinaki.todoapp.data.db.entity.TodoList

class TodoListRepository(context: Context) {
    private val db = TodoDatabase.getInstance(context)
    private val todoListDao = db.todoListDao()

    suspend fun getAllTodoLists() = todoListDao.getAll()

    suspend fun addTodoList(list: TodoList) = todoListDao.addTodoList(list)
}