package dev.pinaki.todoapp.features.todolists

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.viewmodel.BaseViewModel
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.TodoListRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoList

internal class AllListsViewModel(
    application: Application,
    private val todoListRepository: TodoListRepository
) : BaseViewModel(application) {

    val todoLists = todoListRepository.observeAllTodoLists().distinctUntilChanged()

    val showEmptyView = todoLists.map { it.isNullOrEmpty() }

    private val _showTodoList = MutableLiveData<Event<Int>>()
    val showTodoList: LiveData<Event<Int>> = _showTodoList

    fun onTodoListItemClick(item: TodoList) {
        item.id?.run {
            _showTodoList.value = Event(this)
        }
    }

    // TODO: move this to Add Todo Bottom Sheet's View  Model
    fun addTodoList(listName: String, listDescription: String?) {
        val todoList = TodoList(title = listName, description = listDescription)

        launchInIOScope {
            try {
                todoListRepository.addTodoList(todoList)
                _showToast.postValue(Event(R.string.msg_list_added_successfully))
            } catch (e: Exception) {
                _showToast.postValue(Event(R.string.msg_error_occurred))
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
            fragment: TodoListsFragment, repository: TodoListRepository
        ): AllListsViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, repository)
            ).get()
        }
    }
}
