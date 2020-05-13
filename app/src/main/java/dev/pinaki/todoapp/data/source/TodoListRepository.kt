package dev.pinaki.todoapp.data.source

import android.content.Context
import dev.pinaki.todoapp.data.source.local.db.TodoDatabase
import dev.pinaki.todoapp.data.source.local.db.entity.TodoList

class TodoListRepository(context: Context) {
    private val db = TodoDatabase.getInstance(context)
    private val todoListDao = db.todoListDao()
    private val todoDao = db.todoDao()

    suspend fun addTodoList(list: TodoList) = todoListDao.addTodoList(list)

    fun observeAllTodoLists() = todoListDao.observeAllTodoList()

    fun observerTodoListById(id: Int) = todoListDao.observeTodoListById(id)

    suspend fun deleteList(todoList: TodoList) {
        val id = todoList.id ?: return
        todoListDao.deleteTodoList(todoList)
        todoDao.deleteAll(id)
    }
}