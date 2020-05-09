package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
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

    private val todoList: LiveData<TodoList> = _id.distinctUntilChanged().switchMap {
        //TODO: uncomment below comment when we start showing title/description
        /*if (_loading.value == true && todos.value != null)
            _loading.value = false // sloppy way of setting loading = false, find a better way

        */

        todoListRepository.observerTodoListById(it)
    }

    val todos: LiveData<List<TodoItem>> = _id.switchMap {
        //TODO: uncomment below comment when we start showing title/description
        if (_loading.value == true /*&& todoList.value != null*/)
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
                _toast.postValue(dev.pinaki.todoapp.R.string.msg_error_occurred)
            }
        }
    }

    fun onStartDrag(position: Int) {
        startDragPosition = position
        listStateAtStartDrag = todos.value
    }

    fun onDragComplete(position: Int) {
        listStateAtStartDrag?.run {
            startDragPosition?.let {
                if (isTodoSectionChanged(this, it, position)) {
                    // reload list by setting id again
                    _id.value = _id.value
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

    private suspend fun moveItem(item: TodoItem, start: Int, end: Int) {
        todoRepository.moveItem(item, start, end)
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