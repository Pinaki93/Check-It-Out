package dev.pinaki.todoapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import dev.pinaki.todoapp.R
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.databinding.TodoItemBinding
import dev.pinaki.todoapp.ui.adapter.ContentItem
import dev.pinaki.todoapp.ui.adapter.HeaderItem
import dev.pinaki.todoapp.ui.adapter.TodoViewItem
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

@SuppressLint("SimpleDateFormat")
fun Date.getAsString(format: String): String {
    return SimpleDateFormat(format).format(this)
}

fun Date.getAsDisplayString() = getAsString("dd MMM yy, hh:mm a")

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

/**
 * Using this as an extension method instead of a member method
 * as Data Classes don't need to contain Android Specific code
 *
 * @param other: The newer item to compare [this] to
 */
fun TodoViewItem.getChangePayload(other: TodoViewItem): Bundle {
    val thisTodoItem = this@getChangePayload

    if (thisTodoItem is HeaderItem && other is ContentItem || thisTodoItem is ContentItem && other is HeaderItem) {
        Log.d("ChangePayload", "Type mismatch: thisItem: $thisTodoItem, other item: $other")
        return Bundle() // type mismatch
    }

    val bundle = Bundle().apply {
        if (thisTodoItem is HeaderItem && other is HeaderItem) {
            if (thisTodoItem.title != other.title)
                putString(DIFF_UTIL_ARG_TITLE, other.title)

            if (thisTodoItem.count != other.count)
                putInt(DIFF_UTIL_ARG_COUNT, other.count)
        }

        if (thisTodoItem is ContentItem && other is ContentItem) {
            val otherData = other.data
            val thisData = thisTodoItem.data

            if (thisData.title != otherData.title) {
                putString(DIFF_UTIL_ARG_TITLE, otherData.title)
            }

            if (thisData.done != otherData.done) {
                putBoolean(DIFF_UTIL_ARG_DONE, otherData.done)
            }

            if (thisData.dateModified != otherData.dateModified) {
                putLong(DIFF_UTIL_ARG_DATE_MODIFIED, otherData.dateModified.time)
            }

            if (thisData.dateCompeted != otherData.dateCompeted) {
                // let's not do a forced !! check even if
                // there is a very minor chance of a null pointer exception here
                otherData.dateCompeted?.let {
                    putLong(DIFF_UTIL_ARG_DATE_COMPLETED, it.time)
                }
            }
        }
    }

    Log.d(
        "ChangePayload",
        "Returing bundle for: thisItem: $thisTodoItem, other item: $other \n $bundle"
    )

    return bundle
}

fun getViewItems(context: Context, items: List<TodoItem>): MutableList<TodoViewItem> {
    return ArrayList<TodoViewItem>().apply {
        val itemsToDo = ArrayList<TodoViewItem>()
        val itemsCompleted = ArrayList<TodoViewItem>()

        for (item in items) {
            if (item.done)
                itemsCompleted.add(ContentItem(item))
            else
                itemsToDo.add(ContentItem(item))
        }

//        add(HeaderItem(context.getString(R.string.lbl_items_to_do), itemsToDo.size))
        addAll(itemsToDo)

//        add(HeaderItem(context.getString(R.string.lbl_completed_items), itemsCompleted.size))
        addAll(itemsCompleted)
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

fun Int.toPx(context: Context): Int = (this * context.resources.displayMetrics.density).toInt()

fun Int.toDp(context: Context): Int = (this / context.resources.displayMetrics.density).toInt()

fun isSectionChanged(list: List<TodoViewItem>, initialPosition: Int, endPosition: Int): Boolean {
    // index values: -1 => undefined, 0=> to do, 1=> completed
    var currentSection = -1

    var oldSectionOfItem = -1
    var newSectionOfItem = -1

    for (index in list.indices) {
        val item = list[index]

        if (item is HeaderItem) {
            currentSection++
        } else {
            var valueAssigned = false
            if (index == initialPosition) {
                oldSectionOfItem = currentSection
                valueAssigned = true
            } else if (index == endPosition) {
                newSectionOfItem = currentSection
                valueAssigned = true
            }

            if (valueAssigned && newSectionOfItem != -1 && oldSectionOfItem != -1) {
                break
            }
        }
    }

    return oldSectionOfItem != newSectionOfItem
}

/**
 * Returns the start and end positions in absolute list (i.e.: without header items)
 */
fun getDataPositions(
    list: List<TodoViewItem>,
    initialPosition: Int,
    endPosition: Int
): Pair<Int, Int> {
    fun removeHeaderItems(list: List<TodoViewItem>): List<TodoViewItem> {
        return list.filter {
            it !is HeaderItem
        }
    }

    if (list[initialPosition] is HeaderItem)
        throw RuntimeException("Trying to move a header item")

    val initialItem = list[initialPosition]
    val endItem = list[endPosition]

    val cleanList = removeHeaderItems(list)
    return Pair(cleanList.indexOf(initialItem), cleanList.indexOf(endItem))
}

fun TextView.showStrikeThrough(show: Boolean) {
    paintFlags =
        if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}