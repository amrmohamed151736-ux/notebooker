package com.example.data.repository

import com.example.data.local.CustomThemeDao
import com.example.data.local.CustomThemeEntity
import com.example.data.local.FocusSessionDao
import com.example.data.local.FocusSessionEntity
import com.example.data.local.PointsTransactionDao
import com.example.data.local.PointsTransactionEntity
import com.example.data.local.StorePurchaseDao
import com.example.data.local.StorePurchaseEntity
import com.example.focus.SoundEffectManager
import com.example.model.AchievementCatalog
import com.example.model.StoreCatalog
import com.example.model.StoreCategory
import com.example.model.StoreItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FocusRepository(
    private val sessionDao: FocusSessionDao,
    private val transactionDao: PointsTransactionDao,
    private val purchaseDao: StorePurchaseDao,
    private val themeDao: CustomThemeDao
) {
    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.getAllSessions()
    val allTransactions: Flow<List<PointsTransactionEntity>> = transactionDao.getAllTransactions()
    val totalBalance: Flow<Int> = transactionDao.getTotalBalance()
    val allPurchases: Flow<List<StorePurchaseEntity>> = purchaseDao.getAllPurchases()
    val allCustomThemes: Flow<List<CustomThemeEntity>> = themeDao.getAllThemes()

    suspend fun initializeStarterDataIfNeeded() {
        // 1. Check if starter gift has already been granted
        val starterCount = transactionDao.countTransactionsByReference("STARTER_GIFT_INIT", "STARTER_GIFT")
        if (starterCount == 0) {
            transactionDao.insertTransaction(
                PointsTransactionEntity(
                    amount = 50,
                    type = "STARTER_GIFT",
                    description = "هدية بداية التركيز الترحيبية للإنفاق في المتجر",
                    referenceId = "STARTER_GIFT_INIT"
                )
            )
        }

        // 2. Insert default owned items if not already stored
        StoreCatalog.items.filter { it.isDefaultOwned }.forEach { defaultItem ->
            val existing = purchaseDao.getPurchaseByItemId(defaultItem.id)
            if (existing == null) {
                purchaseDao.insertPurchase(
                    StorePurchaseEntity(
                        itemId = defaultItem.id,
                        category = defaultItem.category.name,
                        pricePaid = 0,
                        isEquipped = true
                    )
                )
            }
        }
    }

    /**
     * Records a finished or cancelled focus session and computes points accurately.
     * Rule: 1 point per 60s of actual focus + 10 points bonus if completed.
     * Prevents any double counting by using the session ID as reference.
     */
    suspend fun recordFocusSession(
        sessionId: String = UUID.randomUUID().toString(),
        targetMinutes: Int,
        actualFocusSeconds: Int,
        isCompleted: Boolean
    ): Int {
        val existingSession = sessionDao.getSessionById(sessionId)
        if (existingSession != null) return existingSession.earnedPoints

        // Calculate points
        val minutePoints = (actualFocusSeconds / 60)
        val completionBonus = if (isCompleted) 10 else 0
        val totalSessionPoints = minutePoints + completionBonus

        val sessionEntity = FocusSessionEntity(
            id = sessionId,
            targetDurationMinutes = targetMinutes,
            actualFocusSeconds = actualFocusSeconds,
            isCompleted = isCompleted,
            earnedPoints = totalSessionPoints
        )
        sessionDao.insertSession(sessionEntity)

        // Award minute points if > 0
        if (minutePoints > 0) {
            val countMinutes = transactionDao.countTransactionsByReference(sessionId, "FOCUS_MINUTES")
            if (countMinutes == 0) {
                transactionDao.insertTransaction(
                    PointsTransactionEntity(
                        amount = minutePoints,
                        type = "FOCUS_MINUTES",
                        description = "تركيز فعلي $minutePoints دقيقة",
                        referenceId = sessionId
                    )
                )
            }
        }

        // Award completion bonus if completed
        if (completionBonus > 0) {
            val countBonus = transactionDao.countTransactionsByReference(sessionId, "COMPLETION_BONUS")
            if (countBonus == 0) {
                transactionDao.insertTransaction(
                    PointsTransactionEntity(
                        amount = completionBonus,
                        type = "COMPLETION_BONUS",
                        description = "مكافأة إتمام جلسة $targetMinutes دقيقة بنجاح",
                        referenceId = sessionId
                    )
                )
            }
        }

        // Play feedback sound
        if (isCompleted) {
            SoundEffectManager.playSessionComplete()
        } else if (totalSessionPoints > 0) {
            SoundEffectManager.playPointsEarned()
        }

        // Check achievements trigger
        checkAndAwardAchievements()

        return totalSessionPoints
    }

    suspend fun purchaseStoreItem(item: StoreItem): Result<String> {
        val currentBalance = totalBalance.first()
        if (currentBalance < item.price) {
            return Result.failure(Exception("عذراً، رصيدك الحالي ($currentBalance نقطة) لا يكفي لشراء ${item.titleAr} (${item.price} نقطة)"))
        }

        val existingPurchase = purchaseDao.getPurchaseByItemId(item.id)
        if (existingPurchase != null) {
            return Result.failure(Exception("العنصر مملوك بالفعل لديك!"))
        }

        // Atomic purchase deduction
        transactionDao.insertTransaction(
            PointsTransactionEntity(
                amount = -item.price,
                type = "PURCHASE",
                description = "شراء ${item.titleAr} من المتجر",
                referenceId = item.id
            )
        )

        // Record purchase
        purchaseDao.insertPurchase(
            StorePurchaseEntity(
                itemId = item.id,
                category = item.category.name,
                pricePaid = item.price,
                isEquipped = (item.category == StoreCategory.TIMER_SKINS || item.category == StoreCategory.BADGES_TITLES)
            )
        )

        SoundEffectManager.playItemPurchased()

        // Check if buying this unlocked an achievement
        checkAndAwardAchievements()

        return Result.success("تم شراء ${item.titleAr} بنجاح!")
    }

    suspend fun equipItem(itemId: String, categoryName: String) {
        purchaseDao.unequipAllInCategory(categoryName)
        purchaseDao.equipItem(itemId)
        SoundEffectManager.playPointsEarned()
    }

    suspend fun toggleFavorite(purchase: StorePurchaseEntity) {
        purchaseDao.updatePurchase(purchase.copy(isFavorite = !purchase.isFavorite))
    }

    suspend fun saveCustomTheme(theme: CustomThemeEntity) {
        themeDao.insertTheme(theme)
        checkAndAwardAchievements()
    }

    suspend fun deleteCustomTheme(theme: CustomThemeEntity) {
        themeDao.deleteTheme(theme)
    }

    private suspend fun checkAndAwardAchievements() {
        val sessions = sessionDao.getAllSessions().first()
        val completedCount = sessions.count { it.isCompleted }
        val totalMinutes = sessions.sumOf { it.actualFocusSeconds } / 60
        val purchases = purchaseDao.getAllPurchases().first()
        val collectiblesCount = purchases.count { it.category == StoreCategory.COLLECTIBLES.name }
        val customThemes = themeDao.getAllThemes().first().size

        for (ach in AchievementCatalog.achievements) {
            val alreadyAwarded = transactionDao.countTransactionsByReference(ach.id, "ACHIEVEMENT_REWARD") > 0
            if (alreadyAwarded) continue

            val isUnlocked = when (ach.conditionType) {
                "COMPLETED_SESSIONS" -> completedCount >= ach.targetValue
                "TOTAL_MINUTES" -> totalMinutes >= ach.targetValue
                "COLLECTIBLES_OWNED" -> collectiblesCount >= ach.targetValue
                "THEMES_CREATED" -> customThemes >= ach.targetValue
                else -> false
            }

            if (isUnlocked) {
                transactionDao.insertTransaction(
                    PointsTransactionEntity(
                        amount = ach.rewardPoints,
                        type = "ACHIEVEMENT_REWARD",
                        description = "إنجاز مفتوح: ${ach.titleAr}",
                        referenceId = ach.id
                    )
                )
                SoundEffectManager.playAchievementUnlocked()
            }
        }
    }
}
