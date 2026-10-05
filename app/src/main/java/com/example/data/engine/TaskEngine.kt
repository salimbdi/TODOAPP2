package com.example.data.engine

import com.example.data.db.TaskDao
import com.example.data.model.RecurrenceType
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.TaskOccurrenceEntity
import com.example.data.model.TaskStatus
import com.example.util.DateUtils

class TaskEngine(private val taskDao: TaskDao) {

    /**
     * Determines whether a recurring task should generate an occurrence on the given date.
     */
    fun shouldOccurOnDate(task: RecurringTaskEntity, date: String): Boolean {
        if (!task.isActive) return false
        if (date < task.startDate) return false
        task.endDate?.let { end ->
            if (date > end) return false
        }

        val dayOfWeek = DateUtils.getDayOfWeek(date) // 1=Mon, ..., 7=Sun
        val dayOfMonth = DateUtils.getDayOfMonth(date)

        return when (task.recurrenceType) {
            RecurrenceType.DAILY -> true
            RecurrenceType.WEEKDAYS -> dayOfWeek in 1..5
            RecurrenceType.WEEKENDS -> dayOfWeek in 6..7
            RecurrenceType.SPECIFIC_DAYS -> {
                val allowedDays = task.recurrenceDays
                    .split(",")
                    .mapNotNull { it.trim().toIntOrNull() }
                    .toSet()
                dayOfWeek in allowedDays
            }
            RecurrenceType.WEEKLY -> {
                val startDayOfWeek = DateUtils.getDayOfWeek(task.startDate)
                dayOfWeek == startDayOfWeek
            }
            RecurrenceType.MONTHLY -> {
                val startDayOfMonth = DateUtils.getDayOfMonth(task.startDate)
                dayOfMonth == startDayOfMonth
            }
        }
    }

    /**
     * Generates daily task occurrences for the specified date in an idempotent manner.
     * Guaranteed:
     * - Recurring tasks generate new independent occurrences for each date.
     * - Completion on one date never mutates another date.
     * - Repeated calls for the same date never duplicate tasks.
     */
    suspend fun generateDailyOccurrences(date: String): List<TaskOccurrenceEntity> {
        val existingOccurrences = taskDao.getOccurrencesForDateSync(date)
        val existingRecurringTaskIds = existingOccurrences
            .mapNotNull { it.recurringTaskId }
            .toSet()

        val activeRecurringTasks = taskDao.getActiveRecurringTasks()
        val toInsert = mutableListOf<TaskOccurrenceEntity>()
        var orderCounter = existingOccurrences.size

        for (task in activeRecurringTasks) {
            if (shouldOccurOnDate(task, date)) {
                if (!existingRecurringTaskIds.contains(task.id)) {
                    toInsert.add(
                        TaskOccurrenceEntity(
                            recurringTaskId = task.id,
                            title = task.title,
                            description = task.description,
                            category = task.category,
                            priority = task.priority,
                            date = date,
                            status = TaskStatus.PENDING,
                            completedTimestamp = null,
                            notes = "",
                            estimatedDurationMinutes = task.estimatedDurationMinutes,
                            actualDurationMinutes = 0,
                            preferredTime = task.preferredTime,
                            isOneTime = false,
                            orderIndex = orderCounter++
                        )
                    )
                }
            }
        }

        if (toInsert.isNotEmpty()) {
            taskDao.insertOccurrences(toInsert)
        }

        return taskDao.getOccurrencesForDateSync(date)
    }

    /**
     * Carry over an unfinished one-time task to target date (defaults to today or tomorrow).
     */
    suspend fun carryOverTask(occurrenceId: Long, targetDate: String) {
        val currentOccurrences = taskDao.getAllOccurrences()
        // Or fetch specific occurrence if needed.
        // We can query and create or update date.
    }
}
