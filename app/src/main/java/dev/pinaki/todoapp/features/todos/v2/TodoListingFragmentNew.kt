package dev.pinaki.todoapp.features.todos.v2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.adapter.OnItemInteractionListener
import dev.pinaki.todoapp.common.ui.adapter.TouchHelperCallback
import dev.pinaki.todoapp.common.ui.fragment.BaseFragment
import dev.pinaki.todoapp.common.util.*
import dev.pinaki.todoapp.data.ds.Event
import dev.pinaki.todoapp.data.source.TodoListRepository
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.databinding.TodoListBinding

class TodoListingFragmentNew : BaseFragment<TodoListBinding>(), OnItemInteractionListener {

    private lateinit var isKeyboardOpen: IsKeyboardOpen

    private lateinit var addViewModel: AddTodoViewModel
    private lateinit var listingViewModel: TodosViewModel

    private lateinit var todosAdapter: TodosAdapter

    override fun getLayout() = R.layout.layout_todo_listing_new

    override fun getBinding(
        inflater: LayoutInflater,
        layout: Int,
        parent: ViewGroup?
    ): TodoListBinding = DataBindingUtil.inflate(inflater, layout, parent, false)

    override fun initializeViewModels() {
        val todoRepository = TodoRepository(context!!)
        addViewModel = AddTodoViewModel.newInstance(this, todoRepository)
        listingViewModel =
            TodosViewModel.getInstance(this, todoRepository, TodoListRepository(context!!))
    }

    override fun initializeView() {
        val binding = getBindingInstance()

        binding.run {
            this.viewModel = listingViewModel
            this.isKeyboardOpen = isKeyboardOpen
            this.lifecycleOwner = this@TodoListingFragmentNew

            executePendingBindings()
        }

        todosAdapter = TodosAdapter(listingViewModel)
        binding.rvItems.run {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = todosAdapter

            val touchHelperCallback =
                ItemTouchHelper(TouchHelperCallback(this@TodoListingFragmentNew, this))
            touchHelperCallback.attachToRecyclerView(this)
        }

        binding.addTodoItemView.init(addViewModel, this)
    }

    override fun loadData() {
        val listId: Int = arguments?.getInt(ARG_TODO_LIST_ID)
            ?: throw IllegalStateException("Started listing screen without a valid list id")

        addViewModel.start(listId)
        listingViewModel.start(arguments?.getInt(ARG_TODO_LIST_ID) ?: 0)
    }

    override fun observeData() {
        val binding = getBindingInstance()

        listingViewModel.showDeleteSnackBar.observe(this, Observer { item ->
            Snackbar
                .make(binding.root, getString(R.string.msg_item_deleted), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo)) {
                    listingViewModel.restoreTodoItem(item)
                }.show()
        })

        listingViewModel.showAddTodoView.observe(this, Observer {
            if (it.hasBeenHandled) return@Observer

            showAddTodoView()
        })

        val toastObserver = Observer<Event<Int>> {
            if (it.hasBeenHandled) return@Observer

            it.getContentIfNotHandled()?.let { id ->
                toast(getString(id))
            }
        }
        addViewModel.showToast.observe(this, toastObserver)
        listingViewModel.showToast.observe(this, toastObserver)

        isKeyboardOpen.observe(this, Observer { keyboardOpen ->
            if (!keyboardOpen) {
                binding.addTodoItemView.gone()
                binding.fabAddTodo.visible()
            } else {
                binding.addTodoItemView.visible()
                binding.fabAddTodo.gone()
                binding.addTodoItemView.showKeyboard() // make sure the focus is on the edittext
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            isKeyboardOpen = IsKeyboardOpen(it)
        }
    }

    override fun onPause() {
        super.onPause()
        activity?.hideKeyboard()
    }

    private fun showAddTodoView() {
        activity?.forceShowKeyboard()
    }

    override fun onMove(recyclerView: RecyclerView, initialPosition: Int, finalPosition: Int) {
        todosAdapter.moveItem(initialPosition, finalPosition)
    }

    override fun onItemSelected(recyclerView: RecyclerView, position: Int) {
        listingViewModel.onStartDrag(position)
    }

    override fun onItemReleased(recyclerView: RecyclerView, position: Int) {
        listingViewModel.onDragComplete(position)
    }

    override fun onSwipeLeft(recyclerView: RecyclerView, position: Int) {
        listingViewModel.deleteItem(position)
    }

    override fun onSwipeRight(recyclerView: RecyclerView, position: Int) {
        //TODO: later on, we can add labels or mark as important using this
    }

    companion object {
        const val TAG = "TodoListingFragmentNew"

        private const val ARG_TODO_LIST_ID = "todo_list_id"

        fun newInstance(id: Int): TodoListingFragmentNew {
            return TodoListingFragmentNew().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TODO_LIST_ID, id)
                }
            }
        }
    }
}