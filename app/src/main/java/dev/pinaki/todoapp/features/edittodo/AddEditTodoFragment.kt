package dev.pinaki.todoapp.features.edittodo

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.common.ui.fragment.BaseFragment
import dev.pinaki.todoapp.common.util.IsKeyboardOpen
import dev.pinaki.todoapp.common.util.showAlertDialog
import dev.pinaki.todoapp.data.source.TodoRepository
import dev.pinaki.todoapp.databinding.AddEditTodoBinding

/**
 *TODO:
 * [x]: glue code in Container
 * [ ] handle back button
 * [ ]: handle save
 *
 */
class AddEditTodoFragment : BaseFragment<AddEditTodoBinding>() {

    private lateinit var viewModel: AddEditTodoViewModel

    private lateinit var isKeyboardOpen: IsKeyboardOpen

    override fun getLayout() = R.layout.activity_add_edit_todo

    override fun getBinding(
        inflater: LayoutInflater,
        layout: Int,
        parent: ViewGroup?
    ): AddEditTodoBinding {
        return DataBindingUtil.inflate(inflater, layout, parent, false)
    }

    override fun initializeViewModels() {
        viewModel = AddEditTodoViewModel.getInstance(this, TodoRepository(requireContext()))
    }

    override fun initializeView() {
        val binding = getBindingInstance()
        binding.run {
            viewModel = this@AddEditTodoFragment.viewModel
            lifecycleOwner = this@AddEditTodoFragment
            executePendingBindings()
        }
    }

    override fun loadData() {
        val args = arguments ?: throw Exception("Fragment initialized without setting todo id")
        viewModel.init(args.getInt(ARG_TODO_ID))
    }

    override fun observeDataAndActions() {
        val binding = getBindingInstance()

        isKeyboardOpen.observe(this, Observer { open ->
            if (open) {
                binding.bottomAppBar.performHide()
            } else {
                binding.bottomAppBar.performShow()
            }
        })

        viewModel.showAlertDialog.observe(this, Observer {
            val item = it.getContentIfNotHandled() ?: return@Observer
            showAlertDialog(item = item)
        })

        viewModel.showInfoBottomSheet.observe(this, Observer {
            val todoItem = it.getContentIfNotHandled() ?: return@Observer
            val infoBottomSheet =
                TodoInfoBottomSheet.getInstance(
                    todoItem.dateCreated,
                    todoItem.dateModified,
                    todoItem.dateCompeted
                )
            infoBottomSheet.show(requireActivity().supportFragmentManager, TodoInfoBottomSheet.TAG)
        })
    }

    override fun getToolbarInstance(): Toolbar? {
        return getBindingInstance().bottomAppBar
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity?.let {
            isKeyboardOpen = IsKeyboardOpen(it)
        }
    }

    override fun fragmentHasOptionsMenu() = true

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_edit_todo_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                return true
            }

            R.id.menu_item_delete -> {
                viewModel.onDelete()
                return true
            }

            R.id.menu_item_info -> {
                viewModel.onInfoClicked()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val ARG_TODO_ID = "todo_id"

        fun newInstance(id: Int): AddEditTodoFragment {
            return AddEditTodoFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_TODO_ID, id)
                }
            }
        }
    }
}