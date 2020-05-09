package dev.pinaki.todoapp.data.db.entity

import android.os.Parcelable
import android.text.TextUtils
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import dev.pinaki.todoapp.ds.ComparableItem
import kotlinx.android.parcel.Parcelize
import java.util.*

@Entity(tableName = "todo_item")
@Parcelize
data class TodoItem(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "item_description")
    var description: String? = null,

    @ColumnInfo(name = "is_done")
    var done: Boolean,

    @ColumnInfo(name = "date_created")
    var dateCreated: Date = Date(),

    @ColumnInfo(name = "date_modified")
    var dateModified: Date = Date(),

    @ColumnInfo(name = "date_competed")
    var dateCompeted: Date? = null,

    @ColumnInfo(name = "item_order")
    var itemOrder: Double = 0.0,

    @ColumnInfo(name = "list_ref_id")
    var listRefId: Int = 0
) : ComparableItem<TodoItem>, Parcelable {
    override fun isItemSame(other: TodoItem) = this.id == other.id

    /**
     * Right now I think it's fine to use equals for content checking
     */
    override fun isContentSame(other: TodoItem) = this.equals(other)

    fun isDescriptionPresent() = !TextUtils.isEmpty(description)
}