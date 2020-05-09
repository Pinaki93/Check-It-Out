package dev.pinaki.todoapp.data.source.local.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TodoListWithItems(
    @Embedded val todoList: TodoList,

    @Relation(
        parentColumn = "id",
        entityColumn = "list_ref_id"
    )
    val items: List<TodoItem>
)