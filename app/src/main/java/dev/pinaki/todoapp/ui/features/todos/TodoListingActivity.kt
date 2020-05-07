package dev.pinaki.todoapp.ui.features.todos

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import dev.pinaki.todoapp.BuildConfig
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.databinding.TodoListingActivityBinding
import dev.pinaki.todoapp.util.dbbrowser.DatabaseBrowserActivity
import dev.pinaki.todoapp.util.gone
import dev.pinaki.todoapp.util.visible

class TodoListingActivity : AppCompatActivity() {

    lateinit var binding: TodoListingActivityBinding

    private lateinit var todoViewModel: TodoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.setContentView(this, R.layout.activity_todo_listing)

        todoViewModel =
            TodoViewModel.instance(this, application, TodoRepository(applicationContext))

        supportFragmentManager.beginTransaction()
            .add(
                R.id.content,
                TodoListingFragment()
            )
            .commitAllowingStateLoss()

        if (BuildConfig.DEBUG) {
            binding.btnDbBrowser.visible()
        } else {
            binding.btnDbBrowser.gone()
        }

        binding.btnDbBrowser.setOnClickListener {
            val intent = Intent(this, DatabaseBrowserActivity::class.java)
            startActivity(intent)
        }
    }
}