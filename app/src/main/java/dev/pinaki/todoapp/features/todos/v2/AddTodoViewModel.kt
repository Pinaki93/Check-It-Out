package dev.pinaki.todoapp.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.common.util.launchInIOScope
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem

class AddTodoViewModel(application: Application, val todoRepository: TodoRepository) :
    AndroidViewModel(application) {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val showDescription = MutableLiveData<Boolean>()

    private val _showKeyboardOnTitleField = MediatorLiveData<Boolean>().also {
        it.addSource(showDescription) { value ->
            if (!value) {
                it.value = true
            }
        }
    }
    val showKeyboardOnTitleField: LiveData<Boolean> = _showKeyboardOnTitleField

    private val _showKeyboardOnDescriptionField = MediatorLiveData<Boolean>().also {
        it.addSource(showDescription) { value ->
            it.value = value
        }
    }
    val showKeyboardOnDescriptionField: LiveData<Boolean> = _showKeyboardOnDescriptionField

    private val _listId = MutableLiveData<Int>()

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
        val listId = _listId.value ?: return

        val titleStr: String? = title.value
        val descriptionStr: String? = if (showDescription.value == true) description.value else null

        if (titleStr.isNullOrEmpty()) {
            //show toast
            return
        }

        launchInIOScope {
            todoRepository.addTodo(
                TodoItem(
                    title = titleStr,
                    description = descriptionStr,
                    listRefId = listId,
                    done = false
                ), true
            )

            title.postValue("")
            description.postValue("")
            _showKeyboardOnTitleField.postValue(true)
        }
    }

    fun start(listId: Int) {
        _listId.value = listId
    }

    class Factory(
        private val fragment: TodoListingFragmentNew,
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
            fragment: TodoListingFragmentNew,
            repository: TodoRepository
        ) = ViewModelProviders.of(
            fragment,
            Factory(fragment, repository)
        ).get(AddTodoViewModel::class.java)
    }
}