package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {
    @Query("SELECT * FROM focus_sessions ORDER BY timestampMs DESC")
    fun getAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: String): FocusSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: FocusSessionEntity)
}

@Dao
interface PointsTransactionDao {
    @Query("SELECT * FROM points_transactions ORDER BY timestampMs DESC")
    fun getAllTransactions(): Flow<List<PointsTransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM points_transactions")
    fun getTotalBalance(): Flow<Int>

    @Query("SELECT COUNT(*) FROM points_transactions WHERE referenceId = :referenceId AND type = :type")
    suspend fun countTransactionsByReference(referenceId: String, type: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: PointsTransactionEntity)
}

@Dao
interface StorePurchaseDao {
    @Query("SELECT * FROM store_purchases ORDER BY purchasedAtMs DESC")
    fun getAllPurchases(): Flow<List<StorePurchaseEntity>>

    @Query("SELECT * FROM store_purchases WHERE itemId = :itemId LIMIT 1")
    suspend fun getPurchaseByItemId(itemId: String): StorePurchaseEntity?

    @Query("SELECT * FROM store_purchases WHERE category = :category AND isEquipped = 1 LIMIT 1")
    suspend fun getEquippedByCategory(category: String): StorePurchaseEntity?

    @Query("UPDATE store_purchases SET isEquipped = 0 WHERE category = :category")
    suspend fun unequipAllInCategory(category: String)

    @Query("UPDATE store_purchases SET isEquipped = 1 WHERE itemId = :itemId")
    suspend fun equipItem(itemId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: StorePurchaseEntity)

    @Update
    suspend fun updatePurchase(purchase: StorePurchaseEntity)
}

@Dao
interface CustomThemeDao {
    @Query("SELECT * FROM custom_themes ORDER BY createdAtMs DESC")
    fun getAllThemes(): Flow<List<CustomThemeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTheme(theme: CustomThemeEntity)

    @Delete
    suspend fun deleteTheme(theme: CustomThemeEntity)
}
