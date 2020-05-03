package dev.pinaki.todoapp.ds

import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import dev.pinaki.todoapp.ui.base.diffutil.base.BaseDiffUtilCallback

/**
 * A Util interface to let the comparison logic required
 * for [DiffUtil.Callback] be implemented in the models required for [RecyclerView]
 */
interface ComparableItem<T> {

    /**
     * Called by the [BaseDiffUtilCallback] to decide whether two object represent the same Item.
     * <p>
     * For example, if your items have unique ids, this method should check their id equality.
     */
    fun isItemSame(other: T): Boolean


    /**
     * Called by the [BaseDiffUtilCallback] when it wants to check whether two items have the same data.
     * DiffUtil uses this information to detect if the contents of an item has changed.
     * <p>
     * [BaseDiffUtilCallback] uses this method to check equality instead of {@link Object#equals(Object)}
     * so that you can change its behavior depending on your UI.
     * For example, if you are using DiffUtil with a
     * [RecyclerView.Adapter], you should
     * return whether the items' visual representations are the same.
     * <p>
     * This method is called only if [isItemSame] returns
     * true for these items.
     */
    fun isContentSame(other: T): Boolean
}