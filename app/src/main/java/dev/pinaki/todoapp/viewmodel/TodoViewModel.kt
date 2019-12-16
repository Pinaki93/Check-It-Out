package dev.pinaki.todoapp.viewmodel

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.ds.Event
import dev.pinaki.todoapp.ds.Result
import kotlinx.coroutines.launch

class TodoViewModel(
    application: Application,
    private val repository: TodoRepository
) : AndroidViewModel(application) {

    private val _todos: MutableLiveData<List<TodoItem>> = MutableLiveData()
    val todos: LiveData<List<TodoItem>>
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
            _todos.postValue(repository.getAll())
        }
    }

    fun addTodo(todoItem: TodoItem) {
        launchInIOScope {
            _addTodoResult.postValue(Event(Result.Loading))

            try {
                repository.addTodo(todoItem)
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
                repository.moveItem(todoItem, end, start)
                _moveTodoResult.postValue(Event(Result.Success()))
            } catch (e: Exception) {
                e.printStackTrace()
                _moveTodoResult.postValue(Event(Result.Error(e)))
            } finally {
                loadTodos()
            }
        }
    }

    private inline fun launchInIOScope(crossinline action: suspend () -> Unit) {
        viewModelScope.launch {
            action()
        }
    }

    companion object {
        fun instance(
            activity: FragmentActivity,
            application: Application,
            todoRepository: TodoRepository
        ): TodoViewModel {
            return ViewModelProviders.of(
                activity,
                TodoViewModelFactory(application, todoRepository)
            ).get(TodoViewModel::class.java)
        }
    }
}

class TodoViewModelFactory(
    private val application: Application,
    private val todoRepository: TodoRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return TodoViewModel(application, todoRepository) as T
    }
}