package dev.pinaki.todoapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodoListingBinding
import dev.pinaki.todoapp.ds.Result
import dev.pinaki.todoapp.ui.adapter.TodoItemAdapter
import dev.pinaki.todoapp.ui.swipe.LeftRightFullSwipeCallback
import dev.pinaki.todoapp.ui.swipe.OnSwipeCallback
import dev.pinaki.todoapp.util.getDoneItems
import dev.pinaki.todoapp.util.getTodoItems
import dev.pinaki.todoapp.util.toast
import dev.pinaki.todoapp.viewmodel.TodoViewModel
import java.util.*
import kotlin.collections.ArrayList

class TodoListingFragment : Fragment(), OnSwipeCallback {

    private lateinit var todoViewModel: TodoViewModel

    private lateinit var todoItemsAdapter: TodoItemAdapter
    private lateinit var completedItemsAdapter: TodoItemAdapter

    private lateinit var todoListingBinding: TodoListingBinding

    private val onListItemClickListener: (TodoItem) -> Unit = {
        it.done = !it.done

        // update the completion date
        if (it.done) {
            it.dateCompeted = Date()
        } else {
            it.dateCompeted = null
        }

        todoViewModel.saveTodoItem(it)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        todoListingBinding =
            DataBindingUtil.inflate(inflater, R.layout.layout_todo_listing, container, false)
        return todoListingBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpRecyclerView()

        todoViewModel = TodoViewModel.instance(
            activity!!,
            activity!!.application,
            TodoRepository(activity!!.application)
        )

        todoViewModel.loadTodos()

        todoViewModel.todos.observe(this, Observer {
            todoItemsAdapter.updateItems(getTodoItems(it))
            completedItemsAdapter.updateItems(getDoneItems(it))
        })

        todoViewModel.saveTodoResult.observe(this, Observer {
            when (val contentIfNotHandled: Result<Any>? = it.getContentIfNotHandled()) {
                is Result.Error -> {
                    contentIfNotHandled.cause.printStackTrace()
                    toast(getString(R.string.msg_error_occurred))
                }
            }
        })

        todoViewModel.deleteTodoResult.observe(this, Observer {
            when (val contentIfNotHandled: Result<TodoItem>? = it.getContentIfNotHandled()) {
                is Result.Error -> {
                    toast(getString(R.string.msg_error_occurred))
                    contentIfNotHandled.cause.printStackTrace()
                }

                is Result.Success<TodoItem> -> {
                    // success
                    contentIfNotHandled.data?.let {
                        //showing undo snackbar
                        showUndoDeleteSnackbar(contentIfNotHandled.data)
                    }
                }
            }
        })
    }

    private fun showUndoDeleteSnackbar(data: TodoItem) {
        Snackbar
            .make(todoListingBinding.root, "Item Deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                todoViewModel.addTodo(data)
            }.show()
    }

    private fun setUpRecyclerView() {
        todoListingBinding.rvTodoItems.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        todoListingBinding.rvCompletedItems.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        todoListingBinding.rvTodoItems.isNestedScrollingEnabled = false
        todoListingBinding.rvCompletedItems.isNestedScrollingEnabled = false

        todoItemsAdapter = TodoItemAdapter(
            application = activity!!.application,
            items = ArrayList()
        )
        completedItemsAdapter = TodoItemAdapter(
            application = activity!!.application,
            items = ArrayList()
        )

        todoListingBinding.rvTodoItems.adapter = todoItemsAdapter
        todoListingBinding.rvCompletedItems.adapter = completedItemsAdapter

        todoItemsAdapter.onItemClick = onListItemClickListener
        completedItemsAdapter.onItemClick = onListItemClickListener

        val todoItemTouchHelper = ItemTouchHelper(
            LeftRightFullSwipeCallback(
                activity!!, this,
                todoListingBinding.rvTodoItems,
                R.drawable.ic_delete_white_24dp,
                ContextCompat.getColor(activity!!, R.color.deleteColor),
                R.drawable.ic_mode_edit_white_24dp,
                ContextCompat.getColor(activity!!, R.color.favColor)
            )
        )

        val completedItemTouchHelper = ItemTouchHelper(
            LeftRightFullSwipeCallback(
                activity!!, this,
                todoListingBinding.rvCompletedItems,
                R.drawable.ic_delete_white_24dp,
                ContextCompat.getColor(activity!!, R.color.deleteColor),
                R.drawable.ic_mode_edit_white_24dp,
                ContextCompat.getColor(activity!!, R.color.favColor)
            )
        )

        todoItemTouchHelper.attachToRecyclerView(todoListingBinding.rvTodoItems)
        completedItemTouchHelper.attachToRecyclerView(todoListingBinding.rvCompletedItems)
    }

    override fun onSwipeLeft(recyclerView: RecyclerView, position: Int) {
        val itemSwiped: TodoItem? = when (recyclerView) {
            todoListingBinding.rvTodoItems -> {
                todoItemsAdapter.getItemAtPosition(position)
            }

            todoListingBinding.rvCompletedItems -> {
                completedItemsAdapter.getItemAtPosition(position)
            }

            else -> {
                null
            }
        }

        if (itemSwiped != null) {
            todoViewModel.deleteItem(itemSwiped)
        } else {
            //TODO: Log it
        }

    }

    override fun onSwipeRight(recyclerView: RecyclerView, position: Int) {
        TODO("Not implemented right swipe yet")
    }
}