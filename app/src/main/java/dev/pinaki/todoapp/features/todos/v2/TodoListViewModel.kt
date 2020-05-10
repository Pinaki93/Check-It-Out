package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.util.isTodoSectionChanged
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.source.TodoListRepository
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem
import dev.pinaki.todoapp.data.source.local.db.entity.TodoList
import java.util.*

class TodoListViewModel(
    application: Application,
    private val todoRepository: TodoRepository,
    private val todoListRepository: TodoListRepository
) : AndroidViewModel(application) {

    private val _id = MutableLiveData<Int>()

    val todoList: LiveData<TodoList> = _id.distinctUntilChanged().switchMap {
        todoListRepository.observerTodoListById(it)
    }

    val todos: LiveData<List<TodoItem>> = _id.switchMap {
        if (_loading.value == true)
            _loading.value = false // sloppy way of setting loading = false, find a better way

        todoRepository.observerTodosForList(it)
    }.map {
        val itemsMap: Map<Boolean, List<TodoItem>> = it.groupBy { item -> item.done }
        val orderedList = ArrayList<TodoItem>()

        // add incomplete tasks followed by complete ones
        itemsMap[false]?.run {
            orderedList.addAll(this)
        }
        itemsMap[true]?.run {
            orderedList.addAll(this)
        }

        orderedList
    }

    val empty: LiveData<Boolean> = todos.map { it.isNullOrEmpty() }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _toast = MutableLiveData<Int>()
    val toast: LiveData<Int> = _toast

    private val _showDeleteSnackBar = MutableLiveData<TodoItem>()
    val showDeleteSnackBar: LiveData<TodoItem> = _showDeleteSnackBar

    private var startDragPosition: Int? = null
    private var listStateAtStartDrag: List<TodoItem>? = null

    fun start(id: Int) {
        _loading.value = true
        this._id.value = id
    }

    fun onItemDone(item: TodoItem, done: Boolean) {
        launchInIOScope {
            try {
                todoRepository.updateTodo(item.copy().apply {
                    this.done = done
                    if (done) {
                        this.dateCompeted = Date()
                    }
                })
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast()
            }
        }
    }

    fun onStartDrag(position: Int) {
        startDragPosition = position
        listStateAtStartDrag = todos.value
    }

    fun onDragComplete(position: Int) {
        if (startDragPosition == null || startDragPosition == position || position == -1) return// user is probably swiping

        listStateAtStartDrag?.run {
            startDragPosition?.let {
                if (isTodoSectionChanged(this, it, position)) {
                    // reload list by setting id again
                    reLoadTodos()
                    listStateAtStartDrag = null
                    startDragPosition = null
                } else {
                    launchInIOScope {
                        moveItem(this[it], it, position)
                    }
                }
            }
        }
    }

    private fun reLoadTodos() {
        _id.value = _id.value
    }

    private suspend fun moveItem(item: TodoItem, start: Int, end: Int) {
        try {
            todoRepository.moveItem(item, start, end)
        } catch (e: Exception) {
            e.printStackTrace()
            reLoadTodos()
            showErrorToast()
        } finally {
            listStateAtStartDrag = null
            startDragPosition = null
        }
    }

    fun deleteItem(position: Int) {
        val todos = todos.value

        if (todos.isNullOrEmpty()) throw IllegalStateException("Attempting delete on null list!!")

        launchInIOScope {
            val itemToDelete = todos[position]

            try {
                todoRepository.deleteTodo(itemToDelete)
                _showDeleteSnackBar.postValue(itemToDelete)
            } catch (e: Exception) {
                e.printStackTrace()
                reLoadTodos()
                showErrorToast()
            }
        }
    }

    fun restoreTodoItem(item: TodoItem) {
        launchInIOScope {
            try {
                todoRepository.addTodo(item, false)
            } catch (e: Exception) {
                e.printStackTrace()
                showErrorToast()
            }
        }
    }

    private fun showErrorToast() {
        _toast.postValue(R.string.msg_error_occurred)
    }

    @Suppress("UNCHECKED_CAST")
    internal class Factory(
        private val application: Application,
        private val todoRepository: TodoRepository,
        private val todoListRepository: TodoListRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodoListViewModel(
                application = application,
                todoRepository = todoRepository,
                todoListRepository = todoListRepository
            ) as T
        }
    }

    companion object {
        fun getInstance(
            fragment: TodoListingFragmentNew,
            todoRepository: TodoRepository,
            todoListRepository: TodoListRepository
        ): TodoListViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, todoRepository, todoListRepository)
            ).get(TodoListViewModel::class.java)
        }
    }
}