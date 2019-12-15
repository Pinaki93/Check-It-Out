package dev.pinaki.todoapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodoItemBinding
import java.text.SimpleDateFormat
import java.util.*

fun View.gone() {
    visibility = View.GONE
}

@SuppressLint("SimpleDateFormat")
fun Date.getAsString(format: String): String {
    return SimpleDateFormat(format).format(this)
}

fun Date.getAsDisplayString() = getAsString("dd MMM YY, hh:mm a")

fun getDoneItems(allItems: List<TodoItem>): List<TodoItem> {
    return allItems.filter {
        it.done
    }
}

fun getTodoItems(allItems: List<TodoItem>): List<TodoItem> {
    return allItems.filter {
        !it.done
    }
}

fun Activity?.toast(message: String) {
    if (this != null) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

fun Fragment?.toast(message: String) {
    this?.activity?.toast(message)
}

/**
 * Using this as an extension method instead of a member method
 * as Data Classes don't need to contain Android Specific code
 *
 * @param other: The newer item to compare [this] to
 */
fun TodoItem.getChangePayload(other: TodoItem): Bundle {
    val thisTodoItem = this
    return Bundle().apply {
        if (thisTodoItem.title != other.title) {
            putString(DIFF_UTIL_ARG_TITLE, other.title)
        }

        if (thisTodoItem.done != other.done) {
            putBoolean(DIFF_UTIL_ARG_DONE, other.done)
        }

        if (thisTodoItem.dateModified != other.dateModified) {
            putLong(DIFF_UTIL_ARG_DATE_MODIFIED, other.dateModified.time)
        }

        if (thisTodoItem.dateCompeted != other.dateCompeted) {
            // let's not do a forced !! check even if
            // there is a very minor chance of a null pointer exception here
            other.dateCompeted?.let {
                putLong(DIFF_UTIL_ARG_DATE_COMPLETED, it.time)
            }
        }
    }
}

fun Bundle?.isNotEmpty() = this?.isEmpty != true

fun getTodoItemBinding(parent: ViewGroup): TodoItemBinding =
    DataBindingUtil.inflate(
        LayoutInflater.from(parent.context),
        R.layout.layout_todo_item,
        parent,
        false
    )