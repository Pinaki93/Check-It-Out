package dev.pinaki.todoapp.features.todos.obsolete

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.ds.Result
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem
import java.util.*

class TodoViewModel(
    application: Application,
    private val repository: TodoRepository
) : AndroidViewModel(application) {

    private val _todos: MutableLiveData<Event<Result<List<TodoItem>>>> = MutableLiveData()
    val todos: LiveData<Event<Result<List<TodoItem>>>>
        get() = _todos

    private var _addTodoResult: MutableLiveData<Event<Result<Any>>> = MutableLiveData()
    val addTodoResult: LiveData<Event<Result<Any>>>
        get() = _addTodoResult

    private var _saveTodoResult: MutableLiveData<Event<Result<Any>>> = MutableLiveData()
    val saveTodoResult: LiveData<Event<Result<Any>>>
        get() = _saveTodoResult

    private var _deleteTodoResult: MutableLiveData<Event<Result<TodoItem>>> = MutableLiveData()
    val deleteTodoResult: LiveData<Event<Result<TodoItem>>>
        get() = _deleteTodoResult

    private var _moveTodoResult: MutableLiveData<Event<Result<TodoItem>>> = MutableLiveData()
    val moveTodoResult: LiveData<Event<Result<TodoItem>>>
        get() = _moveTodoResult


    fun loadTodos() {
        launchInIOScope {
            try {
                _todos.postValue(Event(Result.Loading))

                val allTodos = repository.getAll()
                _todos.postValue(Event(Result.Success(allTodos)))
            } catch (e: Exception) {
                _todos.postValue(Event(Result.Error(e)))
            }
        }
    }

    fun addTodo(todoItem: TodoItem, updateOrderId: Boolean = true) {
        launchInIOScope {
            _addTodoResult.postValue(Event(Result.Loading))

            try {
                repository.addTodo(todoItem, updateOrderId)
                _addTodoResult.postValue(Event(Result.Success()))
            } catch (e: Exception) {
                e.printStackTrace()
                _addTodoResult.postValue(Event(Result.Error(e)))
            } finally {
                loadTodos()
            }
        }
    }

    fun saveTodoItem(todoItem: TodoItem) {
        launchInIOScope {
            try {
                _saveTodoResult.postValue(Event(Result.Loading))
                repository.updateTodo(todoItem)

                _saveTodoResult.postValue(Event(Result.Success()))
            } catch (e: Exception) {
                _addTodoResult.postValue(Event(Result.Error(e)))
            } finally {
                loadTodos()
            }
        }
    }

    fun deleteItem(item: TodoItem) {
        launchInIOScope {
            _deleteTodoResult.postValue(Event(Result.Loading))
            try {
                repository.deleteTodo(item)
                _deleteTodoResult.postValue(Event(Result.Success(item)))
            } catch (e: Exception) {
                _deleteTodoResult.postValue(Event(Result.Error(e)))
            } finally {
                loadTodos()
            }
        }
    }

    fun moveItem(start: Int, end: Int, todoItem: TodoItem) {
        launchInIOScope {
            _moveTodoResult.postValue(Event(Result.Loading))
            try {
                repository.moveItem(todoItem, start, end)
                _moveTodoResult.postValue(Event(Result.Success()))
            } catch (e: Exception) {
                e.printStackTrace()
                _moveTodoResult.postValue(Event(Result.Error(e)))
            } finally {
                loadTodos()
            }
        }
    }

    fun addOrSaveTodo(item: TodoItem?, task: String, taskDescription: String, taskDone: Boolean) {
        if (item == null) {
            addTodo(
                TodoItem(
                    title = task,
                    done = taskDone,
                    description = if (taskDescription.isNotEmpty()) taskDescription else null,
                    dateCompeted = if (taskDone) Date() else null,
                    listRefId = 1  /*TODO: this is DANGEROUS. Fix this fast*/
                )
            )
        } else {
            saveTodoItem(item.apply {
                title = task
                description = taskDescription
                done = taskDone

                if (done) {
                    dateCompeted = Date()
                }
            })
        }
    }

    fun userEditedAnything(
        item: TodoItem?,
        task: String,
        taskDescription: String?,
        taskDone: Boolean
    ): Boolean {
        val safeItem = item ?: return true

        return safeItem.title != task
                || safeItem.description != taskDescription
                || safeItem.done != taskDone
    }

    companion object {
        fun instance(
            activity: FragmentActivity,
            application: Application,
            todoRepository: TodoRepository
        ): TodoViewModel {
            return ViewModelProviders.of(
                activity,
                TodoViewModelFactory(
                    application,
                    todoRepository
                )
            ).get(TodoViewModel::class.java)
        }
    }
}

class TodoViewModelFactory(
    private val application: Application,
    private val todoRepository: TodoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TodoViewModel(
            application,
            todoRepository
        ) as T
    }
}