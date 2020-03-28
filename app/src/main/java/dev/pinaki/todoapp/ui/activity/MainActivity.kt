package dev.pinaki.todoapp.ui.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.bus.TodoBus
import dev.pinaki.todoapp.bus.TodoBusItem
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.databinding.TodoListingActivityBinding
import dev.pinaki.todoapp.ui.fragment.TodoListingFragment
import dev.pinaki.todoapp.ui.bottomsheet.AddTodoItemBottomSheet
import dev.pinaki.todoapp.viewmodel.TodoViewModel

class MainActivity : AppCompatActivity() {

    lateinit var binding: TodoListingActivityBinding

    private lateinit var todoViewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)


//        binding.toolbarLayout.run {
//            if (supportActionBar == null) {
//                setSupportActionBar(binding.toolbarLayout)
//            } else {
//                visibility = View.GONE
//            }
//        }

        binding.fabAddTodoItem.setOnClickListener {
            AddTodoItemBottomSheet.show(supportFragmentManager)
        }

        todoViewModel =
            TodoViewModel.instance(this, application, TodoRepository(applicationContext))

//        TodoBus.observe(this, Observer {
//            when (it.eventId) {
//                TodoBusItem.EVENT_TODO_ITEM_ADDED -> {
//                    loadTodos()
//                }
//            }
//        })

        supportFragmentManager.beginTransaction()
            .add(
                R.id.content,
                TodoListingFragment()
            )
            .commitAllowingStateLoss()
    }

    override fun onResume() {
        super.onResume()
        loadTodos()
    }

    private fun loadTodos() {
        todoViewModel.loadTodos()
    }
}
