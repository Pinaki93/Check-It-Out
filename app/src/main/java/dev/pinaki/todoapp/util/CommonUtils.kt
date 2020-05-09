package dev.pinaki.todoapp.util

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.ui.features.todos.adapter.ContentItem
import dev.pinaki.todoapp.ui.features.todos.adapter.HeaderItem
import dev.pinaki.todoapp.ui.features.todos.adapter.TodoViewItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt


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

fun getViewItems(items: List<TodoItem>): MutableList<TodoViewItem> {
    val sortedItems = items.map {
        ContentItem(it)
    }

    return ArrayList(sortedItems)
}

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

fun isTodoSectionChanged(list: List<TodoItem>, initialPosition: Int, endPosition: Int): Boolean {
    return list[initialPosition].done != list[endPosition].done
}

fun TextView.showStrikeThrough(show: Boolean) {
    paintFlags =
        if (show) paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        else paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
}

/////////////////////////////////////////////////////////////////////////////////////
// Util methods for determining if keyboard is open or closed
/////////////////////////////////////////////////////////////////////////////////////
fun Activity.getRootView(): View {
    return findViewById<View>(Window.ID_ANDROID_CONTENT)
}

fun Context.convertDpToPx(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        this.resources.displayMetrics
    )
}

fun Activity.isKeyboardOpen(): Boolean {
    val visibleBounds = Rect()
    this.getRootView().getWindowVisibleDisplayFrame(visibleBounds)
    val heightDiff = getWindowHeight() - visibleBounds.height()
    val marginOfError = convertDpToPx(50F).roundToInt()

    return heightDiff > marginOfError
}

fun Context.getWindowHeight(): Int {
    val displayMetrics = DisplayMetrics()
    val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    windowManager.defaultDisplay.getMetrics(displayMetrics)

    return displayMetrics.heightPixels
}

fun Context.showKeyboard(field: EditText) {
    val inputMethodManager: InputMethodManager? =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    inputMethodManager?.showSoftInput(field, InputMethodManager.SHOW_IMPLICIT)

    field.requestFocus()
}

fun Fragment.showKeyboard(field: EditText) {
    context?.showKeyboard(field)
}

fun Activity?.canShowDialog() = this != null && !this.isFinishing

inline fun ViewModel.launchInIOScope(crossinline action: suspend () -> Unit) {
    viewModelScope.launch(Dispatchers.IO) {
        action()
    }
}