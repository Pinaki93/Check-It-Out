package dev.pinaki.todoapp.features.edittodo

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.model.AlertDialogItem
import dev.pinaki.todoapp.common.model.ButtonItem
import dev.pinaki.todoapp.common.ui.viewmodel.BaseViewModel
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem

class AddEditTodoViewModel(private val todoRepository: TodoRepository, application: Application) :
    BaseViewModel(application) {

    private var todoId: Int? = null
    private var todo: TodoItem? = null

    //  two way data binding
    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()

    // actions dispatchers
    private val _showInfoBottomSheet = MutableLiveData<Event<TodoItem>>()
    val showInfoBottomSheet: LiveData<Event<TodoItem>> = _showInfoBottomSheet

    fun init(todoId: Int) {
        this.todoId = todoId
        this.todoId?.let {
            launchInIOScope {
                todo = todoRepository.getTodo(it)
                todo?.let {
                    title.value = it.title
                    description.value = it.description
                }
            }
        }
    }

    // listeners
    fun onInfoClicked() {
        title.value = "Chicken"
        description.value = "Biriyani"
        todo?.let {
            _showInfoBottomSheet.value = Event(it)
        }
    }

    fun onDelete() {
        _showAlertDialog.value = Event(getDeleteConfirmationDialogItem())
    }

    private fun getDeleteConfirmationDialogItem(): AlertDialogItem {
        return AlertDialogItem(
            title = R.string.delete_dialog_title,
            message = R.string.delete_dialog_message,
            positiveButtonItem = ButtonItem(text = R.string.yes, listener = {
                return@ButtonItem true
            }),
            negativeButtonItem = ButtonItem(text = R.string.no)
        )
    }

    internal class Factory(
        private val application: Application,
        private val todoRepository: TodoRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddEditTodoViewModel(
                application = application,
                todoRepository = todoRepository
            ) as T
        }
    }

    companion object {
        fun getInstance(
            fragment: AddEditTodoFragment,
            todoRepository: TodoRepository
        ): AddEditTodoViewModel {
            return ViewModelProviders.of(
                fragment,
                Factory(fragment.activity!!.application, todoRepository)
            ).get(AddEditTodoViewModel::class.java)
        }
    }
}