package dev.pinaki.todoapp.ui.base.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.ds.ComparableItem
import dev.pinaki.todoapp.ui.base.diffutil.base.BaseDiffUtilCallback

/**
 * We will always be using the BaseDiffUtilCallback class for computing Diff
 * so it makes no sense passing it as a parameter everywhere
 */
abstract class BaseAdapter<T : ComparableItem<T>, ViewHolder : RecyclerView.ViewHolder>
    : ListAdapter<T, ViewHolder>(BaseDiffUtilCallback<T>())