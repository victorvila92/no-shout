package com.example.todeveu.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [EventEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun eventDao(): EventDao
}

private var dbInstance: AppDatabase? = null

fun createAppDatabase(context: Context): AppDatabase {
    return dbInstance ?: Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "todeveu_db"
    )
        .fallbackToDestructiveMigration()
        .build()
        .also { dbInstance = it }
}
