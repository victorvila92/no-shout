package com.example.todeveu.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class EventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val dbRelatiu: Float,
    val similarityScore: Float,
    val vadScore: Float,
    val tipusEvent: String,
    val sustainMs: Int,
    val cooldownMs: Int,
    val dbThreshold: Float,
    val speakerThreshold: Float,
    val vadThreshold: Float,
)
