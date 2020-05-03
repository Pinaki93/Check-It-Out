package dev.pinaki.todoapp.ui.features.todolists

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.data.TodoListRepository
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.ds.Empty
import dev.pinaki.todoapp.ds.Event
import dev.pinaki.todoapp.ds.Result
import dev.pinaki.todoapp.util.launchInIOScope

class AllListsViewModel(
    application: Application,
    private val todoListRepository: TodoListRepository
) :
    AndroidViewModel(application) {


    private val _lists: MutableLiveData<Event<Result<List<TodoList>>>> = MutableLiveData()
    val lists: LiveData<Event<Result<List<TodoList>>>>
        get() = _lists


    private val _addListResult: MutableLiveData<Event<Result<Empty>>> = MutableLiveData()
    val addTodoResult: MutableLiveData<Event<Result<Empty>>>
        get() = _addListResult

    fun loadTodoLists() {
        _lists.postValue(Event(Result.Loading))

        launchInIOScope {
            try {
                _lists.postValue(Event(Result.Success(todoListRepository.getAllTodoLists())))
            } catch (e: Exception) {
                _lists.postValue(Event(Result.Error(e)))
            }
        }
    }

    fun addTodoList(listName: String, listDescription: String?) {
        val todoList = TodoList(title = listName, description = listDescription)

        _addListResult.postValue(Event(Result.Loading))
        launchInIOScope {
            try {
                todoListRepository.addTodoList(todoList)
                _addListResult.postValue(Event(Result.Success(Empty)))

                loadTodoLists()
            } catch (e: Exception) {
                _addListResult.postValue(Event(Result.Error(e)))
            }
        }
    }

    internal class Factory(
        private val application: Application,
        private val todoListRepository: TodoListRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AllListsViewModel(
                application = application,
                todoListRepository = todoListRepository
            ) as T
        }
    }

    companion object {
        fun getInstance(
            fragment: AllListsFragment,
            repository: TodoListRepository
        ): AllListsViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, repository)
            ).get()
        }
    }
}
