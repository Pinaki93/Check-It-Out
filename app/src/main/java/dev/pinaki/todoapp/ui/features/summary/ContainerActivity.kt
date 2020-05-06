package dev.pinaki.todoapp.ui.features.summary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.ui.features.todolists.AllListsFragment
import dev.pinaki.todoapp.ui.features.todos.TodoListingFragment

class ContainerActivity : AppCompatActivity(), AllListsFragment.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        supportFragmentManager.beginTransaction()
            .add(R.id.container, AllListsFragment.newInstance())
            .commitNow()
    }

    override fun showTodoDetails(id: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, TodoListingFragment.newInstance(id))
            .addToBackStack(null)
            .commit()
    }
}