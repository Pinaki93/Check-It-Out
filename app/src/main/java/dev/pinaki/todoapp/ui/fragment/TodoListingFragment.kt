package dev.pinaki.todoapp.ui.fragment

import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
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
import dev.pinaki.todoapp.util.*
import dev.pinaki.todoapp.viewmodel.TodoViewModel
import java.util.*
import kotlin.collections.ArrayList

class TodoListingFragment : Fragment(), OnItemInteractionListener, View.OnClickListener,
    TextView.OnEditorActionListener {

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

    private val keyboardOpenCloseListener = object : ViewTreeObserver.OnGlobalLayoutListener {
        private var lastState: Boolean = activity?.isKeyboardOpen() ?: false

        override fun onGlobalLayout() {
            val isOpen = activity?.isKeyboardOpen() ?: false
            if (isOpen == lastState) {
                return
            } else {
                lastState = isOpen
                onKeyboardStateChange(isOpen)
            }
        }
    }

    private val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private val onKeyboardStateChange = { open: Boolean ->
        prepareAddTodoSection(open && todoListingBinding.etTodoItem.hasFocus())
    }

    private val smoothScroller: LinearSmoothScroller by lazy {
        object : LinearSmoothScroller(context!!) {
            override fun getVerticalSnapPreference() = SNAP_TO_START
        }
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

        initUI()

        todoViewModel = TodoViewModel.instance(
            activity!!,
            activity!!.application,
            TodoRepository(activity!!.application)
        )

        todoViewModel.todos.observe(this, Observer {
            when (val contentIfNotHandled: Result<List<TodoItem>>? = it.getContentIfNotHandled()) {
                is Result.Loading -> {
                    if (adapter.items.isEmpty()) {
                        showLoaderView(true)
                        showContentView(false)
                    }
                }

                is Result.Success -> {
                    showLoaderView(false)
                    showContentView(true)

                    contentIfNotHandled.data?.let { list ->
                        adapter.items = getViewItems(list)
                    }
                }

                is Result.Error -> {
                    contentIfNotHandled.cause.printStackTrace()
                    toast(getString(R.string.msg_error_occurred))
                }
            }
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

        todoViewModel.addTodoResult.observe(this, Observer {
            when (val contentIfNotHandled: Result<Any>? = it.getContentIfNotHandled()) {
                is Result.Error -> {
                    toast(getString(R.string.msg_error_occurred))
                    contentIfNotHandled.cause.printStackTrace()
                }

                is Result.Success<Any> -> {
                    // success
                    todoListingBinding.etTodoItem.text.clear()
                    smoothScrollToTop()
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

        showLoaderView(true)
        showContentView(false)
        todoViewModel.loadTodos()
    }

    override fun onResume() {
        super.onResume()
        activity!!.getRootView().viewTreeObserver.addOnGlobalLayoutListener(
            keyboardOpenCloseListener
        )
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            activity!!.getRootView().viewTreeObserver.removeOnGlobalLayoutListener(
                keyboardOpenCloseListener
            )
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // Callbacks
    /////////////////////////////////////////////////////////////////////////////////////
    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_add_todo_item -> {
                addTodoItem()
            }
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (v?.equals(todoListingBinding.etTodoItem) == true) {
            addTodoItem()
            return true
        }

        return false
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
        if (startDragPosition == -1 || position == -1 || startDragPosition == position) {
            startDragPosition = -1
            return
        }

        if (isSectionChanged(initialListState, startDragPosition, position)) {
            // no need to re-load the items from the db, use cached list instead
            adapter.items = initialListState
        } else {
            // get the start and end positions in absolute list (i.e.: without header items)
            todoViewModel.moveItem(
                startDragPosition,
                position,
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
    private fun initUI() {
        adapter = TodoListingAdapter(activity?.application!!)
        todoListingBinding.rvItems.adapter = adapter
        todoListingBinding.rvItems.layoutManager = linearLayoutManager

        todoListingBinding.rvItems.isNestedScrollingEnabled = false

        adapter.onItemClick = onListItemClickListener
        adapter.onItemDelete = onListItemDeleteClickListener

        val itemTouchHelper = ItemTouchHelper(
            TodoItemRecyclerViewCallback(
                this, todoListingBinding.rvItems
            )
        )

        itemTouchHelper.attachToRecyclerView(todoListingBinding.rvItems)

        todoListingBinding.btnAddTodoItem.setOnClickListener(this)
        todoListingBinding.etTodoItem.setOnEditorActionListener(this)
    }

    private fun prepareAddTodoSection(showOptions: Boolean) {
        if (showOptions) {
            todoListingBinding.btnAddTodoItem.visible()
        } else {
            todoListingBinding.btnAddTodoItem.gone()
        }
    }

    private fun addTodoItem() {
        val task = todoListingBinding.etTodoItem.text.toString()

        if (task.isNotEmpty())
            todoViewModel.addTodo(TodoItem(title = task, done = false))
        else
            toast(getString(R.string.err_item_name_empty))
    }

    private fun showLoaderView(shouldShow: Boolean) {
        if (shouldShow) {
            todoListingBinding.loaderLayout.visible()
            todoListingBinding.loaderLayout.startShimmer()
        } else {
            todoListingBinding.loaderLayout.gone()
            todoListingBinding.loaderLayout.stopShimmer()
        }
    }

    private fun showContentView(shouldShow: Boolean) {
        if (shouldShow) {
            todoListingBinding.mainLayout.visible()
            todoListingBinding.cvAddItem.visible()
        } else {
            todoListingBinding.mainLayout.gone()
            todoListingBinding.cvAddItem.gone()
        }
    }

    private fun smoothScrollToTop() {
        smoothScroller.targetPosition = 0
        linearLayoutManager.startSmoothScroll(smoothScroller)
    }

    private fun showUndoDeleteSnackbar(data: TodoItem) {
        //TODO: add to strings.xml
        Snackbar
            .make(todoListingBinding.root, "Item Deleted", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                todoViewModel.addTodo(data, false)
            }.show()
    }
}