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
import dev.pinaki.todoapp.ui.adapter.ContentItem
import dev.pinaki.todoapp.ui.adapter.TodoListingAdapter
import dev.pinaki.todoapp.ui.adapter.TodoViewItem
import dev.pinaki.todoapp.ui.adapter.swipeanddrag.OnItemInteractionListener
import dev.pinaki.todoapp.ui.adapter.swipeanddrag.TodoItemRecyclerViewCallback
import dev.pinaki.todoapp.util.getDataPositions
import dev.pinaki.todoapp.util.getViewItems
import dev.pinaki.todoapp.util.isSectionChanged
import dev.pinaki.todoapp.util.toast
import dev.pinaki.todoapp.viewmodel.TodoViewModel
import java.util.*
import kotlin.collections.ArrayList

class TodoListingFragment : Fragment(), OnItemInteractionListener {

    private lateinit var todoViewModel: TodoViewModel

    private lateinit var todoListingBinding: TodoListingBinding
    private lateinit var adapter: TodoListingAdapter

    private var startDragPosition: Int = 0

    private var initialListState: MutableList<TodoViewItem> = ArrayList()

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

    private val onListItemDeleteClickListener: (TodoItem) -> Unit = {
        todoViewModel.deleteItem(it)
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
            adapter.items = getViewItems(context!!, it)
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
            .make(todoListingBinding.root, "Item Deleted", Snackbar.LENGTH_INDEFINITE)
            .setAction("Undo") {
                todoViewModel.addTodo(data)
            }.show()
    }

    private fun setUpRecyclerView() {
        adapter = TodoListingAdapter(activity?.application!!)
        todoListingBinding.rvItems.adapter = adapter
        todoListingBinding.rvItems.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        todoListingBinding.rvItems.isNestedScrollingEnabled = false

        adapter.onItemClick = onListItemClickListener
        adapter.onItemDelete = onListItemDeleteClickListener

        val itemTouchHelper = ItemTouchHelper(
            TodoItemRecyclerViewCallback(
                activity!!, this,
                todoListingBinding.rvItems,
                R.drawable.ic_delete_white_24dp,
                ContextCompat.getColor(activity!!, R.color.deleteColor),
                R.drawable.ic_mode_edit_white_24dp,
                ContextCompat.getColor(activity!!, R.color.favColor)
            )
        )

        itemTouchHelper.attachToRecyclerView(todoListingBinding.rvItems)
    }

    override fun onSwipeLeft(recyclerView: RecyclerView, position: Int) {
        todoViewModel.deleteItem((adapter.items[position] as ContentItem).data)
    }

    override fun onSwipeRight(recyclerView: RecyclerView, position: Int) {
        TODO("Not implemented right swipe yet")
    }

    override fun onMove(recyclerView: RecyclerView, initialPosition: Int, finalPosition: Int) {
        adapter.moveItem(initialPosition, finalPosition)
    }

    override fun onItemSelected(recyclerView: RecyclerView, position: Int) {
        startDragPosition = position

        initialListState.clear()
        initialListState.addAll(adapter.items)
    }

    override fun onItemReleased(recyclerView: RecyclerView, position: Int) {
        if (startDragPosition == -1) {
            //TODO: Log it - inconsistent state
            return
        }

        if (isSectionChanged(initialListState, startDragPosition, position)) {
            // no need to re-load the items from the db, use cached list instead
            adapter.items = initialListState
        } else {
            // get the start and end positions in absolute list (i.e.: without header items)
            val dataPositions = getDataPositions(initialListState, startDragPosition, position)
            todoViewModel.moveItem(
                dataPositions.first,
                dataPositions.second,
                (initialListState[startDragPosition] as ContentItem).data
            )
        }

        // reset it
        startDragPosition = -1
    }
}