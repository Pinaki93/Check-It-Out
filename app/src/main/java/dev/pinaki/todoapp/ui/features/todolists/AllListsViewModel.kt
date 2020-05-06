package dev.pinaki.todoapp.ui.features.todolists

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoListRepository
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.util.launchInIOScope

class AllListsViewModel(
    application: Application,
    private val todoListRepository: TodoListRepository
) :
    AndroidViewModel(application) {

    val todoLists: LiveData<List<TodoList>>
        get() = _todoLists


    private val _todoLists = todoListRepository.observeAllTodoLists()
        .distinctUntilChanged()
        .switchMap {
            _showEmptyView.value = it.isNullOrEmpty()
            return@switchMap MutableLiveData(it)
        }

    private val _showEmptyView = MutableLiveData<Boolean>(true)
    val showEmptyView: LiveData<Boolean>
        get() = _showEmptyView


    val toast = MutableLiveData<Int>()

    fun addTodoList(listName: String, listDescription: String?) {
        val todoList = TodoList(title = listName, description = listDescription)

        launchInIOScope {
            try {
                todoListRepository.addTodoList(todoList)
                toast.postValue(R.string.msg_list_added_successfully)
            } catch (e: Exception) {
                toast.postValue(R.string.msg_error_occurred)
            }
        }
    }

    fun onTodoListItemClick(item: TodoList) {

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
