package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.viewmodel.BaseViewModel
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem

class AddTodoViewModel(application: Application, val todoRepository: TodoRepository) :
    BaseViewModel(application) {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val showDescription = MutableLiveData<Boolean>()

    private var listId: Int = 0

    private val _showKeyboardOnTitleField: MutableLiveData<Boolean> =
        MediatorLiveData<Boolean>().also {
            it.addSource(showDescription) { value ->
                if (!value) {
                    it.value = true
                }
            }
        }
    val showKeyboardOnTitleField: LiveData<Boolean> = _showKeyboardOnTitleField

    private val _showKeyboardOnDescriptionField: MutableLiveData<Boolean> =
        MediatorLiveData<Boolean>().also {
            it.addSource(showDescription) { value ->
                it.value = value
            }
        }
    val showKeyboardOnDescriptionField: LiveData<Boolean> = _showKeyboardOnDescriptionField

    fun start(listId: Int) {
        this.listId = listId
    }

    fun onTitleSubmit() {
        if (showDescription.value == true) {
            _showKeyboardOnDescriptionField.value = true
        } else {
            onContinue()
        }
    }

    fun onDescriptionSubmit() {
        onContinue()
    }

    fun onContinue() {
        val titleStr: String? = title.value
        if (titleStr.isNullOrEmpty()) {
            _showToast.value = Event(R.string.err_item_name_empty)
            return
        }

        val saveDescription = showDescription.value == true && !description.value.isNullOrEmpty()
        val descriptionStr: String? = if (saveDescription) description.value else null

        launchInIOScope {
            try {
                saveTodoItem(titleStr, descriptionStr, listId)
                clearInputFields()
            } catch (e: Exception) {
                e.printStackTrace()
                _showToast.postValue(Event(R.string.msg_error_occurred))
            }
        }
    }

    private suspend fun saveTodoItem(titleStr: String, descriptionStr: String?, listId: Int) {
        todoRepository.addTodo(
            TodoItem(
                title = titleStr,
                description = descriptionStr,
                listRefId = listId,
                done = false
            ),
            updateOrderId = true
        )
    }

    private fun clearInputFields() {
        title.postValue("")
        description.postValue("")
        _showKeyboardOnTitleField.postValue(true)
    }

    class Factory(
        private val fragment: TodosFragment,
        private val repository: TodoRepository
    ) :
        ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return AddTodoViewModel(
                application = fragment.activity!!.application,
                todoRepository = repository
            ) as T
        }
    }

    companion object {
        fun newInstance(
            fragment: TodosFragment,
            repository: TodoRepository
        ) = ViewModelProviders.of(
            fragment,
            Factory(fragment, repository)
        ).get(AddTodoViewModel::class.java)
    }
}