package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.model.AlertDialogItem
import dev.pinaki.todoapp.common.model.ButtonItem
import dev.pinaki.todoapp.common.ui.viewmodel.BaseViewModel
import dev.pinaki.todoapp.common.util.isTodoSectionChanged
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.TodoListRepository
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem
import dev.pinaki.todoapp.data.source.local.db.entity.TodoList
import java.util.*

class TodosViewModel(
    application: Application,
    private val todoRepository: TodoRepository,
    private val todoListRepository: TodoListRepository
) : BaseViewModel(application) {

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

    private val _showDeleteSnackBar = MutableLiveData<TodoItem>()
    val showDeleteSnackBar: LiveData<TodoItem> = _showDeleteSnackBar

    private val _showAddTodoView = MutableLiveData<Event<Boolean>>()
    val showAddTodoView: LiveData<Event<Boolean>> = _showAddTodoView

    private val _showTodoListScreen = MutableLiveData<Event<Boolean>>()
    val showTodoListScreen: LiveData<Event<Boolean>> = _showTodoListScreen

    private val _showEditScreen = MutableLiveData<Event<TodoItem>>()
    val showEditScreen: LiveData<Event<TodoItem>> = _showEditScreen

    private var startDragPosition: Int? = null
    private var listStateAtStartDrag: List<TodoItem>? = null

    fun start(id: Int) {
        _loading.value = true
        _id.value = id
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

    fun onItemClick(item: TodoItem) {
        _showEditScreen.value = Event(item)
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

    fun onClearAllOptionClick() {
        _showAlertDialog.value = Event(getClearAllConfirmAlert())
    }

    private fun getClearAllConfirmAlert() =
        AlertDialogItem(
            title = R.string.clear_items_dialog_title,
            message = R.string.delete_dialog_message,
            positiveButtonItem = ButtonItem(R.string.yes) {
                clearAllItems()
                return@ButtonItem true
            },

            negativeButtonItem = ButtonItem(R.string.no)
        )

    private fun clearAllItems() {
        _id.value?.let {
            launchInIOScope {
                try {
                    todoRepository.deleteAll(it)
                    _showToast.postValue(Event(R.string.all_items_deleted))
                } catch (e: Exception) {
                    e.printStackTrace()
                    showErrorToast()
                }
            }
        }
    }

    fun onClearCompletedClick() {
        _showAlertDialog.value = Event(getClearCompletedConfirmAlert())
    }

    private fun getClearCompletedConfirmAlert() =
        AlertDialogItem(
            title = R.string.clear_items_dialog_title,
            message = R.string.delete_dialog_message,
            positiveButtonItem = ButtonItem(R.string.yes) {
                clearCompletedItems()
                return@ButtonItem true
            },

            negativeButtonItem = ButtonItem(R.string.no)
        )

    private fun clearCompletedItems() {
        _id.value?.let {
            launchInIOScope {
                try {
                    todoRepository.deleteCompleted(it)
                    _showToast.postValue(Event(R.string.completed_items_deleted))
                } catch (e: Exception) {
                    e.printStackTrace()
                    showErrorToast()
                }
            }
        }
    }

    fun onDeleteListClick() {
        _showAlertDialog.value = Event(getDeleteListConfirmAlert())
    }

    private fun getDeleteListConfirmAlert() =
        AlertDialogItem(
            title = R.string.clear_items_dialog_title,
            message = R.string.delete_dialog_message,
            positiveButtonItem = ButtonItem(R.string.yes) {
                deleteList()
                return@ButtonItem true
            },

            negativeButtonItem = ButtonItem(R.string.no)
        )

    private fun deleteList() {
        todoList.value?.let {
            launchInIOScope {
                try {
                    todoListRepository.deleteList(it)
                    _showToast.postValue(Event(R.string.list_deleted))
                    _showTodoListScreen.postValue(Event(true))
                } catch (e: Exception) {
                    e.printStackTrace()
                    showErrorToast()
                }
            }
        }
    }

    private fun showErrorToast() {
        _showToast.postValue(Event(R.string.msg_error_occurred))
    }

    @Suppress("UNCHECKED_CAST")
    internal class Factory(
        private val application: Application,
        private val todoRepository: TodoRepository,
        private val todoListRepository: TodoListRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodosViewModel(
                application = application,
                todoRepository = todoRepository,
                todoListRepository = todoListRepository
            ) as T
        }
    }

    companion object {
        fun getInstance(
            fragment: TodosFragment,
            todoRepository: TodoRepository,
            todoListRepository: TodoListRepository
        ): TodosViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, todoRepository, todoListRepository)
            ).get(TodosViewModel::class.java)
        }
    }
}