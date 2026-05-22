package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_hooks")
data class SavedHook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val niche: String,
    val hookType: String,
    val platform: String,
    val hookText: String,
    val videoScenario: String,
    val ctaText: String,
    val timestamp: Long = System.currentTimeMillis()
)
