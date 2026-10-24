package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [NotebookEntity::class, TaskEntity::class],
    version = 3,
    exportSchema = false
)
abstract class NoteForgeDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var INSTANCE: NoteForgeDatabase? = null

        fun getDatabase(context: Context): NoteForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NoteForgeDatabase::class.java,
                    "noteforge_database.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
