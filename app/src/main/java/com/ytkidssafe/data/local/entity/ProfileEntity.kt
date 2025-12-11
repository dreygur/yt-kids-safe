package com.ytkidssafe.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatar: String,
    val dailyLimitMinutes: Int = 60,
    val usedTodayMinutes: Int = 0,
    val lastResetDate: Long = System.currentTimeMillis(),
    val categoryFilters: String = "[]" // JSON array
)
