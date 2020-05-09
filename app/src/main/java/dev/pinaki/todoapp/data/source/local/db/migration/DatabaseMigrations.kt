package dev.pinaki.todoapp.data.source.local.db.migration

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

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