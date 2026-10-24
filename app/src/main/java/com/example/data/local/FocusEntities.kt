package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val targetDurationMinutes: Int,
    val actualFocusSeconds: Int,
    val isCompleted: Boolean,
    val earnedPoints: Int,
    val timestampMs: Long = System.currentTimeMillis()
)

@Entity(tableName = "points_transactions")
data class PointsTransactionEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val timestampMs: Long = System.currentTimeMillis(),
    val amount: Int, // Positive for gains, negative for purchases
    val type: String, // STARTER_GIFT, FOCUS_MINUTES, COMPLETION_BONUS, PURCHASE, ACHIEVEMENT_REWARD
    val description: String,
    val referenceId: String? = null // Session ID or Item ID to prevent duplicate crediting
)

@Entity(tableName = "store_purchases")
data class StorePurchaseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val category: String,
    val pricePaid: Int,
    val purchasedAtMs: Long = System.currentTimeMillis(),
    val isEquipped: Boolean = false,
    val isFavorite: Boolean = false
)

@Entity(tableName = "custom_themes")
data class CustomThemeEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val primaryColorHex: String,
    val secondaryColorHex: String,
    val backgroundColorHex: String,
    val textColorHex: String,
    val surfaceColorHex: String,
    val cornerRadiusDp: Int = 16,
    val createdAtMs: Long = System.currentTimeMillis(),
    val isDefault: Boolean = false
)
