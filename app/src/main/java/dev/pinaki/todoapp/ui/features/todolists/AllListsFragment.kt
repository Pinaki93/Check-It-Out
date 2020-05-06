package dev.pinaki.todoapp.ui.features.todolists

import android.content.Context
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

    private var listener: Listener? = null

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

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val containerActivity = activity
        if (containerActivity is Listener) {
            listener = containerActivity
        }
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

        viewModel.showTodoList.observe(this, Observer {
            it.getContentIfNotHandled()?.run {
                listener?.showTodoDetails(this)
            }
        })
    }

    interface Listener {
        fun showTodoDetails(id: Int)
    }

    override fun onAddTodoList(listName: String, listDescription: String?) {
        viewModel.addTodoList(listName, listDescription)
    }

    companion object {
        const val TAG = "AllListFragment"

        fun newInstance() = AllListsFragment()
    }
}
