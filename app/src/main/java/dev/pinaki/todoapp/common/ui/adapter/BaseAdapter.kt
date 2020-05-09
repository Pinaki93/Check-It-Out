package dev.pinaki.todoapp.common.ui.adapter

import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.common.ui.diffutil.base.BaseDiffUtilCallback
import dev.pinaki.todoapp.data.ds.ComparableItem

/**
 * We will always be using the BaseDiffUtilCallback class for computing Diff
 * so it makes no sense passing it as a parameter everywhere
 */
abstract class BaseAdapter<T : ComparableItem<T>, ViewHolder : RecyclerView.ViewHolder>
    : ListAdapter<T, ViewHolder>(BaseDiffUtilCallback<T>())