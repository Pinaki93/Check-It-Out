package dev.pinaki.todoapp.ui.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.TodoRepository
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.AddEditTodoBinding
import dev.pinaki.todoapp.ui.bottomsheet.TodoInfoBottomSheet
import dev.pinaki.todoapp.util.KeyboardUtil
import dev.pinaki.todoapp.util.canShowDialog
import dev.pinaki.todoapp.util.showStrikeThrough
import dev.pinaki.todoapp.util.toast
import dev.pinaki.todoapp.viewmodel.TodoViewModel


class AddEditTodoActivity : AppCompatActivity(), CompoundButton.OnCheckedChangeListener,
    View.OnClickListener {

    private lateinit var binding: AddEditTodoBinding
    private lateinit var todoViewModel: TodoViewModel
    private var exitConfirmationDialog: AlertDialog? = null

    private var item: TodoItem? = null

    private val keyboardUtil: KeyboardUtil by lazy {
        KeyboardUtil(this).apply {
            onKeyboardStateChangeListener = this@AddEditTodoActivity.onKeyboardStateChangeListener
        }
    }

    private val onKeyboardStateChangeListener: (Boolean) -> Unit = { open: Boolean ->
        if (open) {
            supportActionBar?.hide()
        } else {
            supportActionBar?.show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_edit_todo)
        initUI()

        todoViewModel = TodoViewModel.instance(
            this,
            application,
            TodoRepository(application)
        )

        item = intent?.extras?.getParcelable(EXTRA_ITEM)
        item?.run {
            binding.etTodoItem.setText(title)
            binding.etTodoItemDescription.setText(description)
            binding.cbTodoDone.isChecked = done

            binding.etTodoItem.showStrikeThrough(done)
        }
    }

    override fun onResume() {
        super.onResume()
        keyboardUtil.listenToKeyboardChanges(true)
    }

    override fun onPause() {
        super.onPause()
        keyboardUtil.listenToKeyboardChanges(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.add_edit_todo_menu, menu)
        menu?.findItem(R.id.menu_item_delete)?.isVisible = item != null

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.menu_item_delete -> {
                deleteItem()
                return true
            }

            R.id.menu_item_info -> {
                showInfo()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (userEditedAnything()) {
            if (!canShowDialog() || exitConfirmationDialog?.isShowing == true) return

            exitConfirmationDialog = AlertDialog.Builder(this)
                .apply {
                    setMessage(getString(R.string.exit_dialog_message))

                    setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.dismiss()
                        saveItem()
                    }

                    setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        dialog.dismiss()
                        super.onBackPressed()
                    }

                    setNeutralButton(getString(R.string.cancel)) { dialog, _ ->
                        dialog.dismiss()
                    }
                }.show()
        } else {
            super.onBackPressed()
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        binding.etTodoItem.showStrikeThrough(isChecked)
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.fab_save) {
            saveItem()
        }
    }

    private fun initUI() {
        setSupportActionBar(binding.bottomAppBar)

        binding.fabSave.setOnClickListener(this)
        binding.cbTodoDone.setOnCheckedChangeListener(this)
    }

    private fun saveItem() {
        val task = binding.etTodoItem.text.toString()
        val taskDescription = binding.etTodoItemDescription.text.toString()
        val taskDone = binding.cbTodoDone.isChecked

        if (task.isEmpty()) {
            toast(getString(R.string.err_item_name_empty))
            return
        }

        if (userEditedAnything()) {
            todoViewModel.addOrSaveTodo(item, task, taskDescription, taskDone)
        }

        finish()
    }

    private fun showInfo() {
        item?.let {
            val infoBottomSheet =
                TodoInfoBottomSheet.getInstance(it.dateCreated, it.dateModified, it.dateCompeted)

            infoBottomSheet.show(supportFragmentManager, TodoInfoBottomSheet.TAG)
        }
    }

    private fun deleteItem() {
        if (!canShowDialog()) return

        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.delete_dialog_title))
            setMessage(getString(R.string.delete_dialog_message))

            setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                item?.let {
                    todoViewModel.deleteItem(it)
                }

                dialog.dismiss()
                finish()
            }

            setNegativeButton(getString(R.string.no)) { dialog, _ ->
                dialog.dismiss()
            }
        }.show()
    }

    private fun userEditedAnything(): Boolean {
        val taskDescription = binding.etTodoItemDescription.text.toString()

        return todoViewModel.userEditedAnything(
            item,
            binding.etTodoItem.text.toString(),
            if (taskDescription.isNotEmpty()) taskDescription else null,
            binding.cbTodoDone.isChecked
        )
    }

    companion object {

        private const val EXTRA_ITEM = "item"

        @JvmStatic
        fun startActivity(fragment: Fragment, item: TodoItem) {
            val intent = Intent(fragment.activity!!, AddEditTodoActivity::class.java).apply {
                putExtra(EXTRA_ITEM, item)
            }

            fragment.startActivity(intent)
        }
    }
}