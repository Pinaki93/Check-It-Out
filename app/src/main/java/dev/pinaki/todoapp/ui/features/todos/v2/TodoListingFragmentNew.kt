package dev.pinaki.todoapp.ui.features.todos.v2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.databinding.TodoListBinding
import dev.pinaki.todoapp.util.IsKeyboardOpen

class TodoListingFragmentNew : Fragment() {

    private lateinit var binding: TodoListBinding
    private lateinit var addViewModel: AddTodoViewModel
    private lateinit var listingViewModel: TodoListViewModel
    private lateinit var isKeyboardOpen: IsKeyboardOpen

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.layout_todo_listing_new, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val todoRepository = TodoRepository(context!!)
        addViewModel = AddTodoViewModel.newInstance(this, todoRepository)
        listingViewModel = TodoListViewModel.getInstance(this, todoRepository)

        binding.run {
            this.viewModel = listingViewModel
            this.isKeyboardOpen = isKeyboardOpen
            this.lifecycleOwner = this@TodoListingFragmentNew

            executePendingBindings()
        }

        binding.rvItems.run {
            this.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = TodosAdapter(listingViewModel)
        }

        binding.addTodoItemView.init(addViewModel, isKeyboardOpen, this)

        val listId = arguments?.getInt(ARG_TODO_LIST_ID) ?: 0
        addViewModel.start(listId)
        listingViewModel.start(arguments?.getInt(ARG_TODO_LIST_ID) ?: 0)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            isKeyboardOpen = IsKeyboardOpen(it)
        }
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