package dev.pinaki.todoapp.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.bus.TodoBus
import dev.pinaki.todoapp.bus.TodoBusItem
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.AddTodoItemBinding
import dev.pinaki.todoapp.ds.Result
import dev.pinaki.todoapp.viewmodel.TodoViewModel

class AddTodoItemBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var binding: AddTodoItemBinding

    private lateinit var todoViewModel: TodoViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_bottom_sheet_add_todo_item, container, false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        todoViewModel = TodoViewModel.instance(
            activity!! as AppCompatActivity,
            activity!!.application,
            TodoRepository(activity?.application!!)
        )

        binding.btnAddTodoItem.setOnClickListener(this)

        todoViewModel.addTodoResult.observe(this, Observer {
            when (val contentIfNotHandled: Result<Any>? = it.getContentIfNotHandled()) {
                is Result.Success<Any> -> {
                    showNormalView()
                    TodoBus.postValue(TodoBusItem(TodoBusItem.EVENT_TODO_ITEM_ADDED))
                    dismissAllowingStateLoss()
                }

                is Result.Loading -> {
                    showLoadingView()
                }

                is Result.Error -> {
                    showNormalView()
                    contentIfNotHandled.cause.printStackTrace()
                }

                // else the  event has  been consumed
            }
        })
    }

    private fun showNormalView() {
        binding.etTodoItem.isEnabled = true

        binding.btnAddTodoItem.text = getString(R.string.button_lbl_save)
        binding.btnAddTodoItem.isEnabled = true
    }

    private fun showLoadingView() {
        binding.etTodoItem.isEnabled = false

        binding.btnAddTodoItem.text = getString(R.string.button_lbl_loading)
        binding.btnAddTodoItem.isEnabled = false
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_todo_item -> {
                val task = binding.etTodoItem.text.toString()
                todoViewModel.addTodo(TodoItem(title = task, done = false))
            }
        }
    }

    companion object {

        const val TAG = "AddTodoItemBottomSheet"

        fun newInstance() =
            AddTodoItemBottomSheet()

        fun show(fragmentManager: FragmentManager) {
            newInstance()
                .show(fragmentManager,
                    TAG
                )
        }
    }
}