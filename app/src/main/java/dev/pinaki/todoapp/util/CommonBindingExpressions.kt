package dev.pinaki.todoapp.util

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("app:GoneUnless")
fun goneUnless(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}