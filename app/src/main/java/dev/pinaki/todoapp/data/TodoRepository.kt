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
        todoDao.add(item)
    }

    suspend fun updateTodo(item: TodoItem) {
        item.dateModified = Date()
        todoDao.update(item)
    }

    suspend fun deleteTodo(item: TodoItem) {
        todoDao.delete(item)
    }
}