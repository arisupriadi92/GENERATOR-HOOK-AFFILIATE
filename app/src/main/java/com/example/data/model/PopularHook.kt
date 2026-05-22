package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "popular_hooks")
data class PopularHook(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val hookText: String,
    val videoScenario: String,
    val ctaText: String,
    val niche: String,
    val hookType: String,
    val platform: String,
    val whyEffective: String,
    val copyCount: Int = 0,
    val shareCount: Int = 0,
    val isUserGenerated: Boolean = false
)
