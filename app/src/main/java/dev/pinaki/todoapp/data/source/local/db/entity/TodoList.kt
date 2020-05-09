package dev.pinaki.todoapp.data.source.local.db.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pinaki.todoapp.data.ds.ComparableItem
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "todo_list")
@Parcelize
data class TodoList(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "item_description")
    var description: String? = null
) : ComparableItem<TodoList>, Parcelable {
    override fun isItemSame(other: TodoList) = this.id == other.id

    /**
     * Right now I think it's fine to use equals for content checking
     */
    override fun isContentSame(other: TodoList) = this == other
}