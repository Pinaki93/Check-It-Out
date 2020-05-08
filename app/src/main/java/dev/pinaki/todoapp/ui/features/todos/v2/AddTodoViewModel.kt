package dev.pinaki.todoapp.ui.features.todos.v2

import android.app.Application
import androidx.lifecycle.*
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.util.launchInIOScope

class AddTodoViewModel(application: Application, val todoRepository: TodoRepository) :
    AndroidViewModel(application) {

    val title = MutableLiveData<String>()
    val description = MutableLiveData<String>()
    val showDescription = MutableLiveData<Boolean>()

    val showKeyboardOnTitleField = MutableLiveData<Boolean>()
    val showKeyboardOnDescriptionField = MutableLiveData<Boolean>()

    private val _listId = MutableLiveData<Int>()

    init {
        showDescription.map {
            showKeyboardOnDescriptionField.value = true
            return@map it
        }
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

    fun onTitleSubmit() {
        if (showDescription.value == true) {
            showKeyboardOnDescriptionField.value = true
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
            showKeyboardOnTitleField.postValue(true)
        }
    }

    fun start(listId: Int) {
        _listId.value = listId
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