package dev.pinaki.todoapp.features.todolists

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.fragment.BaseFragment
import dev.pinaki.todoapp.common.util.toast
import dev.pinaki.todoapp.data.source.TodoListRepository
import dev.pinaki.todoapp.databinding.AllListBinding
import dev.pinaki.todoapp.features.landing.LandingFragment

class TodoListsFragment : BaseFragment<AllListBinding>(),
    AddTodoListBottomSheetDialogFragment.Listener {

    private lateinit var viewModel: AllListsViewModel
    private lateinit var todoListAdapter: TodoListAdapter

    private var listener: Listener? = null

    override fun getLayout() = R.layout.all_lists_fragment

    override fun getBinding(
        inflater: LayoutInflater,
        layout: Int,
        parent: ViewGroup?
    ): AllListBinding {
        return DataBindingUtil.inflate(inflater, getLayout(), parent, false)
    }

    override fun getToolbarInstance(): Toolbar? {
        return null
    }

    override fun initializeViewModels() {
        viewModel =
            AllListsViewModel.getInstance(
                this,
                TodoListRepository(context!!.applicationContext)
            )
    }

    override fun initializeView() {
        val binding = getBindingInstance()

        todoListAdapter = TodoListAdapter(viewModel)
        binding.rvTodoLists.run {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = todoListAdapter
        }

        binding.fabAddList.setOnClickListener {
            binding.fabAddList.hide()

            val bottomSheet = AddTodoListBottomSheetDialogFragment.show(this)
            val dismissListener: (dialog: DialogInterface) -> Unit = {
                binding.fabAddList.show()
            }
            bottomSheet.dialog?.apply {
                setOnDismissListener(dismissListener)
                setOnCancelListener(dismissListener)
            }
        }

        binding.apply {
            viewModel = this@TodoListsFragment.viewModel
            lifecycleOwner = this@TodoListsFragment

            executePendingBindings()
        }
    }

    override fun observeData() {
        viewModel.showToast.observe(this, Observer {
            if (it.hasBeenHandled) return@Observer

            it.getContentIfNotHandled()?.run {
                toast(getString(this))
            }
        })

        viewModel.showTodoList.observe(this, Observer {
            it.getContentIfNotHandled()?.run {
                listener?.showTodoDetails(this)
            }
        })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val containerActivity = activity
        if (containerActivity is Listener) {
            listener = containerActivity
        }
    }

    override fun onAddTodoList(listName: String, listDescription: String?) {
        viewModel.addTodoList(listName, listDescription)
    }

    override fun onAddBottomSheetDismiss() {
        getBindingInstance().fabAddList.show()
    }

    interface Listener {
        fun showTodoDetails(id: Int)
    }

    companion object {
        const val TAG = "AllListFragment"

        fun newInstance(targetFragment: LandingFragment): TodoListsFragment {
            return TodoListsFragment().apply {
                setTargetFragment(targetFragment, 1)
            }
        }
    }
}
