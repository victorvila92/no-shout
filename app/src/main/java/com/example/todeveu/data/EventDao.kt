package com.example.todeveu.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: EventEntity)

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    fun allEvents(): Flow<List<EventEntity>>

    @Query("SELECT * FROM events WHERE timestamp >= :startOfDay ORDER BY timestamp DESC")
    fun eventsToday(startOfDay: Long): Flow<List<EventEntity>>

    @Query("SELECT COUNT(*) FROM events WHERE timestamp >= :startOfDay")
    suspend fun countToday(startOfDay: Long): Int

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentEvents(limit: Int): List<EventEntity>

    @Query("SELECT * FROM events ORDER BY timestamp DESC")
    suspend fun allEventsList(): List<EventEntity>

    @Query("DELETE FROM events")
    suspend fun deleteAll()
}
