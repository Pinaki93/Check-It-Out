package dev.pinaki.todoapp.ui.features.todos.v2

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.data.db.entity.TodoListWithItems
import dev.pinaki.todoapp.util.launchInIOScope
import java.util.*
import kotlin.collections.ArrayList

class TodoListViewModel(
    application: Application,
    private val todoRepository: TodoRepository
) : AndroidViewModel(application) {

    private val _id = MutableLiveData<Int>()

    private val _item: LiveData<TodoListWithItems> = _id.switchMap {
        if (_loading.value == true) {
            _loading.value = false
        }

        todoRepository.observerTodosForList(it)
    }

    val listItem: LiveData<TodoList> = _item.map { it.todoList }
    val todos: LiveData<List<TodoItem>> = _item.map {
        Log.d("ItemTest", "callback: $it")
        ArrayList(it.items)
    }

    val empty: LiveData<Boolean> = todos.map { it.isNullOrEmpty() }

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _toast = MutableLiveData<Int>()
    val toast: LiveData<Int> = _toast

    fun start(id: Int) {
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

    internal class Factory(
        private val application: Application,
        private val todoRepository: TodoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodoListViewModel(
                application = application,
                todoRepository = todoRepository
            ) as T
        }
    }

    companion object {
        fun getInstance(
            fragment: TodoListingFragmentNew,
            todoRepository: TodoRepository
        ): TodoListViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, todoRepository)
            ).get(TodoListViewModel::class.java)
        }
    }
}