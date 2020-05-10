package dev.pinaki.todoapp.features

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.features.todolists.AllListsFragment
import dev.pinaki.todoapp.features.todos.v2.TodoListingFragmentNew

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
            .replace(R.id.container, TodoListingFragmentNew.newInstance(id))
            .addToBackStack(null)
            .commit()
    }
}