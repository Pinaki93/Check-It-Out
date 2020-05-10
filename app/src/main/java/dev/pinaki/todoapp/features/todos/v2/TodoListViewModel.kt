package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.util.isTodoSectionChanged
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
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

    val todoList: LiveData<TodoList> =
        _id.distinctUntilChanged().switchMap { todoListRepository.observerTodoListById(it) }

    val todos: LiveData<List<TodoItem>> =
        _id.switchMap { todoRepository.observerTodosForList(it) }
            .map { list ->
                if (_loading.value == true)
                    _loading.value = false

                val itemsMap: Map<Boolean, List<TodoItem>> = list.groupBy { item -> item.done }
                return@map ArrayList<TodoItem>().apply {
                    itemsMap[false]?.let {
                        addAll(it)
                    }
                    itemsMap[true]?.let {
                        addAll(it)
                    }
                }
            }

    val empty: LiveData<Boolean> = todos.map { it.isNullOrEmpty() }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _toast = MutableLiveData<Int>()
    val toast: LiveData<Int> = _toast

    private val _showDeleteSnackBar = MutableLiveData<TodoItem>()
    val showDeleteSnackBar: LiveData<TodoItem> = _showDeleteSnackBar

    private val _showAddTodoView = MutableLiveData<Event<Boolean>>()
    val showAddTodoView: LiveData<Event<Boolean>> = _showAddTodoView

    private var startDragPosition: Int? = null
    private var listStateAtStartDrag: List<TodoItem>? = null

    fun start(id: Int) {
        _loading.value = true
        this._id.value = id
    }

    fun addTodoItem() {
        _showAddTodoView.value = Event(true)
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