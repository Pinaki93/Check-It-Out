package dev.pinaki.todoapp.common.util

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
import dev.pinaki.todoapp.common.model.AlertDialogItem
import dev.pinaki.todoapp.data.source.local.db.entity.TodoItem
import dev.pinaki.todoapp.features.todos.obsolete.adapter.ContentItem
import dev.pinaki.todoapp.features.todos.obsolete.adapter.HeaderItem
import dev.pinaki.todoapp.features.todos.obsolete.adapter.TodoViewItem
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

fun Context.showKeyboard(field: EditText, forced: Boolean = false) {
    val inputMethodManager: InputMethodManager? =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    val flag = if (forced) InputMethodManager.SHOW_IMPLICIT else InputMethodManager.SHOW_FORCED
    inputMethodManager?.showSoftInput(field, flag)

    field.requestFocus()
}

fun Context.forceShowKeyboard() {
    val inputMethodManager: InputMethodManager? =
        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    inputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
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

fun Activity.hideKeyboard() {
    val imm =
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    //Find the currently focused view, so we can grab the correct window token from it.
    var view = currentFocus
    //If no view currently has focus, create a new one, just so we can grab a window token from it
    if (view == null) {
        view = View(this)
    }
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment?.cantShowDialog() = this == null || this.activity?.isFinishing ?: false

fun Fragment.showAlertDialog(item: AlertDialogItem) {
    if (cantShowDialog()) return

    activity?.let {
        androidx.appcompat.app.AlertDialog.Builder(it).apply {
            if (item.title != null)
                setTitle(getString(item.title))

            if (item.message != null)
                setMessage(item.message)

            setCancelable(item.cancelable)

            if (item.positiveButtonItem != null) {
                setPositiveButton(
                    item.positiveButtonItem.text
                ) { dialog, _ ->
                    if (item.positiveButtonItem.listener == null) {
                        dialog.dismiss()
                        return@setPositiveButton
                    }

                    val dismiss = item.positiveButtonItem.listener.invoke()
                    if (dismiss) {
                        dialog.dismiss()
                    }
                }
            }

            if (item.neutralButtonItem != null) {
                setNeutralButton(
                    item.neutralButtonItem.text
                ) { dialog, _ ->
                    if (item.neutralButtonItem.listener == null) {
                        dialog.dismiss()
                        return@setNeutralButton
                    }

                    val dismiss = item.neutralButtonItem.listener.invoke()
                    if (dismiss) {
                        dialog.dismiss()
                    }
                }
            }

            if (item.negativeButtonItem != null) {
                setNegativeButton(
                    item.negativeButtonItem.text
                ) { dialog, _ ->
                    if (item.negativeButtonItem.listener == null) {
                        dialog.dismiss()
                        return@setNegativeButton
                    }

                    val dismiss = item.negativeButtonItem.listener.invoke()
                    if (dismiss) {
                        dialog.dismiss()
                    }
                }
            }
        }.show()
    }


}