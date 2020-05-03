package dev.pinaki.todoapp.ui.features.todolists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoListRepository
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.databinding.AllListBinding
import dev.pinaki.todoapp.ds.Result
import dev.pinaki.todoapp.util.gone
import dev.pinaki.todoapp.util.toast
import dev.pinaki.todoapp.util.visible

class AllListsFragment : Fragment(), AddTodoListBottomSheetDialogFragment.Listener {

    private lateinit var binding: AllListBinding
    private lateinit var viewModel: AllListsViewModel
    private lateinit var todoListAdapter: TodoListAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.all_lists_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        initViewModel()
        viewModel.loadTodoLists()
    }

    private fun initUI() {
        todoListAdapter = TodoListAdapter()

        binding.rvTodoLists.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = todoListAdapter
        }

        binding.layoutAddTodoList.setOnClickListener {
            AddTodoListBottomSheetDialogFragment.show(this)
        }

        binding.layoutEmptyList.tvDescription.text =
            getString(R.string.msg_todo_lists_empty)
    }

    private fun initViewModel() {
        viewModel =
            AllListsViewModel.getInstance(this, TodoListRepository(context!!.applicationContext))

        viewModel.lists.observe(this, Observer {
            if (it.hasBeenHandled) return@Observer

            it.getContentIfNotHandled()?.run {
                when (this) {
                    is Result.Loading -> {
                        // TODO: show shimmer layout
                    }

                    is Result.Success -> {
                        refreshList(data)
                    }

                    is Result.Error -> {
                        toast(getString(R.string.some_error_occurred))
                    }
                }
            }
        })
    }

    private fun refreshList(list: List<TodoList>?) {
        if (list == null || list.isEmpty()) {
            showEmptyView(true)
        } else {
            showEmptyView(false)
            todoListAdapter.items = list
        }
    }

    private fun showEmptyView(shouldShow: Boolean) {
        if (shouldShow)
            binding.layoutEmptyList.root.visible()
        else
            binding.layoutEmptyList.root.gone()
    }

    companion object {
        fun newInstance() = AllListsFragment()
    }

    override fun onAddTodoList(listName: String, listDescription: String?) {
        viewModel.addTodoList(listName, listDescription)
    }
}
