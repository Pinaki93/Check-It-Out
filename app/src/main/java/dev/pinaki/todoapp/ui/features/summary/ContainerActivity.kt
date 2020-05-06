package dev.pinaki.todoapp.ui.features.summary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.ui.features.todolists.AllListsFragment

class ContainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.summary_activity)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, AllListsFragment.newInstance())
            .commitNow()
    }
}
