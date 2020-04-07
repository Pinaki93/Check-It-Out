package dev.pinaki.todoapp.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.bus.TodoBus
import dev.pinaki.todoapp.bus.TodoBusItem
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
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
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

        TodoBus.observe(this, Observer {
            when (it.eventId) {
                TodoBusItem.EVENT_TODO_ITEM_ADDED -> {
                    todoViewModel.loadTodos()
                }
            }
        })
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    /////////////////////////////////////////////////////////////////////////////////////
    override fun onMove(recyclerView: RecyclerView, initialPosition: Int, finalPosition: Int) {
        adapter.moveItem(initialPosition, finalPosition)
    }

    override fun onItemSelected(recyclerView: RecyclerView, position: Int) {
        startDragPosition = position

        initialListState.clear()
        initialListState.addAll(adapter.items)
    }

    override fun onItemReleased(recyclerView: RecyclerView, position: Int) {
        if (startDragPosition == -1 || position == -1 || startDragPosition == position) {
            startDragPosition = -1
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

    override fun onSwipeLeft(recyclerView: RecyclerView, position: Int) {
        val item = adapter.items[position]
        if (item is ContentItem) {
            todoViewModel.deleteItem(item.data)
        }
    }

    override fun onSwipeRight(recyclerView: RecyclerView, position: Int) {
        todoViewModel.loadTodos()
    }


    /////////////////////////////////////////////////////////////////////////////////////
    // Private Util methods
    /////////////////////////////////////////////////////////////////////////////////////
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
                this, todoListingBinding.rvItems
            )
        )

        itemTouchHelper.attachToRecyclerView(todoListingBinding.rvItems)
    }

    private fun showUndoDeleteSnackbar(data: TodoItem) {
        //TODO: add to strings.xml
        Snackbar
            .make(todoListingBinding.root, "Item Deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                todoViewModel.addTodo(data)
            }.show()
    }
}