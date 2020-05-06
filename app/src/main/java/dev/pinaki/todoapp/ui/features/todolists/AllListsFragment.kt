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
import dev.pinaki.todoapp.databinding.AllListBinding
import dev.pinaki.todoapp.util.toast

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

        viewModel =
            AllListsViewModel.getInstance(this, TodoListRepository(context!!.applicationContext))

        initUI()
        initViewModel()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    private fun initUI() {
        todoListAdapter = TodoListAdapter(viewModel)

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
        viewModel.toast.observe(this, Observer {
            toast(getString(it))
        })
    }

    companion object {
        fun newInstance() = AllListsFragment()
    }

    override fun onAddTodoList(listName: String, listDescription: String?) {
        viewModel.addTodoList(listName, listDescription)
    }
}
