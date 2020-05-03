package dev.pinaki.todoapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.pinaki.todoapp.data.db.TodoDatabase.Companion.DB_VERSION
import dev.pinaki.todoapp.data.db.converter.ComplexDataConverter
import dev.pinaki.todoapp.data.db.dao.TodoDao
import dev.pinaki.todoapp.data.db.dao.TodoListDao
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.data.db.entity.TodoList
import dev.pinaki.todoapp.data.db.migration.migration1to2
import dev.pinaki.todoapp.data.db.migration.migration2to3
import dev.pinaki.todoapp.data.db.migration.migration3to4

@Database(version = DB_VERSION, entities = [TodoItem::class, TodoList::class])
@TypeConverters(ComplexDataConverter::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

    abstract fun todoListDao(): TodoListDao

    companion object {

        //TODO: move these constants to build config
        const val DB_VERSION = 4
        private const val DB_NAME = "todo_db"

        private lateinit var instance: TodoDatabase

        fun getInstance(context: Context): TodoDatabase {
            if (!::instance.isInitialized) {
                synchronized(TodoDatabase::class) {
                    if (!::instance.isInitialized) {
                        instance = Room
                            .databaseBuilder(
                                context.applicationContext,
                                TodoDatabase::class.java,
                                DB_NAME
                            )
                            .addMigrations(migration1to2, migration2to3, migration3to4)
                            .build()
                    }
                }
            }

            return instance
        }
    }
}