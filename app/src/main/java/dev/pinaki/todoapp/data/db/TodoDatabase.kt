package dev.pinaki.todoapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.pinaki.todoapp.data.db.TodoDatabase.Companion.DB_VERSION
import dev.pinaki.todoapp.data.db.converter.ComplexDataConverter
import dev.pinaki.todoapp.data.db.dao.TodoDao
import dev.pinaki.todoapp.data.db.entity.TodoItem
import dev.pinaki.todoapp.data.db.entity.TodoList

@Database(version = DB_VERSION, entities = [TodoItem::class, TodoList::class])
@TypeConverters(ComplexDataConverter::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao

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

//Database Migrations
val migration1to2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE todo_item add COLUMN item_order REAL NOT NULL DEFAULT 0")
        database.execSQL("UPDATE todo_item set item_order=(id+1)")
    }
}

val migration2to3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE todo_item add COLUMN item_description TEXT DEFAULT NULL")
    }
}

val migration3to4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE IF NOT EXISTS `todo_list` (`id` INTEGER PRIMARY KEY AUTOINCREMENT, `title` TEXT NOT NULL, `item_description` TEXT)")
        database.execSQL("INSERT INTO `todo_list` (`id`,`title`) values (1,'My List')")

        database.execSQL("ALTER TABLE todo_item add COLUMN list_ref_id INTEGER NOT NULL DEFAULT 0")
        database.execSQL("UPDATE todo_item set list_ref_id=1")
    }
}