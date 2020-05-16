package dev.pinaki.todoapp.features

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.features.edittodo.AddEditTodoFragment
import dev.pinaki.todoapp.features.landing.LandingFragment
import dev.pinaki.todoapp.features.todolists.TodoListsFragment
import dev.pinaki.todoapp.features.todos.v2.TodosFragment

class ContainerActivity : AppCompatActivity(), TodoListsFragment.Listener, TodosFragment.Listener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        supportFragmentManager.beginTransaction()
            .add(R.id.container, LandingFragment.newInstance())
            .commitNow()
    }

    override fun showTodoDetails(id: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, TodosFragment.newInstance(id))
            .addToBackStack(null)
            .commit()
    }

    override fun showEditScreen(id: Int) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AddEditTodoFragment.newInstance(id))
            .addToBackStack(null)
            .commit()
    }
}