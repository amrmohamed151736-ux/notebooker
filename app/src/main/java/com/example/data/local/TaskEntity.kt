package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "WORK", // WORK, STUDY, PERSONAL, PROJECT, HEALTH
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val dueDate: String = "", // e.g. "2026-10-24" or formatted text
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
