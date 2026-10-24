package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NotebookEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        PointsTransactionEntity::class,
        StorePurchaseEntity::class,
        CustomThemeEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class NoteForgeDatabase : RoomDatabase() {
    abstract fun notebookDao(): NotebookDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun pointsTransactionDao(): PointsTransactionDao
    abstract fun storePurchaseDao(): StorePurchaseDao
    abstract fun customThemeDao(): CustomThemeDao

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
