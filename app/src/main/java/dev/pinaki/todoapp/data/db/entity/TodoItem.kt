package dev.pinaki.todoapp.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pinaki.todoapp.ds.ComparableItem
import java.util.*

@Entity(tableName = "todo_item")
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "is_done")
    var done: Boolean,

    @ColumnInfo(name = "date_created")
    var dateCreated: Date = Date(),

    @ColumnInfo(name = "date_modified")
    var dateModified: Date = Date(),

    @ColumnInfo(name = "date_competed")
    var dateCompeted: Date? = null
) : ComparableItem<TodoItem> {
    override fun isItemSame(other: TodoItem) = this.id == other.id

    /**
     * Right now I think it's fine to use equals for content checking
     */
    override fun isContentSame(other: TodoItem) = this == other
}