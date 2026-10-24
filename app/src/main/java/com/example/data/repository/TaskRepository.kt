package com.example.data.repository

import com.example.data.local.TaskDao
import com.example.data.local.TaskEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(private val dao: TaskDao) {

    val allTasks: Flow<List<TaskEntity>> = dao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = dao.getPendingTasks()

    fun getTasksByCategory(category: String): Flow<List<TaskEntity>> {
        return dao.getTasksByCategory(category)
    }

    suspend fun addTask(
        title: String,
        description: String = "",
        category: String = "WORK",
        priority: String = "MEDIUM",
        dueDate: String = ""
    ): Long {
        val task = TaskEntity(
            title = title.trim(),
            description = description.trim(),
            category = category,
            priority = priority,
            dueDate = dueDate,
            isCompleted = false,
            createdAt = System.currentTimeMillis()
        )
        return dao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        dao.updateTask(task)
    }

    suspend fun toggleTaskCompletion(taskId: Long, currentStatus: Boolean) {
        dao.updateTaskCompletion(taskId, !currentStatus)
    }

    suspend fun deleteTask(taskId: Long) {
        dao.deleteTaskById(taskId)
    }

    suspend fun clearCompletedTasks() {
        dao.clearCompletedTasks()
    }

    suspend fun ensureDefaultTasksSeeded() {
        val initialSample = listOf(
            TaskEntity(
                title = "تجهيز وطباعة دفتر المهام الأسبوعي (Weekly Horizon)",
                description = "اختيار مقاس A4، عدد 30 صفحة، وتطبيق غلاف جلد كلاسيكي فاخر.",
                category = "WORK",
                priority = "HIGH",
                dueDate = "اليوم",
                isCompleted = false
            ),
            TaskEntity(
                title = "تصميم غلاف مزخرف بنقش الأبراج الفلكية والتطريز الذهبي",
                description = "استخدام الاستوديو غير المتصل لتوليد نمط كوكبات سماوية مع أركان نحاسية.",
                category = "PROJECT",
                priority = "HIGH",
                dueDate = "غداً",
                isCompleted = false
            ),
            TaskEntity(
                title = "تحديد أولويات الربع السنوي في مصفوفة أيزنهاور (Eisenhower Matrix)",
                description = "فرز المهام العاجلة والمهمة وتوزيعها على الربع الأول والثاني.",
                category = "STUDY",
                priority = "MEDIUM",
                dueDate = "هذا الأسبوع",
                isCompleted = false
            ),
            TaskEntity(
                title = "مراجعة أهداف اللياقة وقائمة الوجبات الصحية",
                description = "إعداد جدول الوجبات وتتبع استهلاك الماء اليومي.",
                category = "HEALTH",
                priority = "LOW",
                dueDate = "السبت",
                isCompleted = true
            )
        )
        dao.insertTasks(initialSample)
    }
}
