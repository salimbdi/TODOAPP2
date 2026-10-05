package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_tasks")
data class RecurringTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val recurrenceDays: String = "1,2,3,4,5,6,7", // Comma-separated ISO day of week 1..7
    val startDate: String, // YYYY-MM-DD
    val endDate: String? = null,
    val preferredTime: String? = null, // HH:mm
    val estimatedDurationMinutes: Int = 30,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "task_occurrences",
    indices = [
        Index(value = ["recurringTaskId", "date"]),
        Index(value = ["date"])
    ]
)
data class TaskOccurrenceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recurringTaskId: Long? = null, // Null for standalone one-time tasks
    val title: String,
    val description: String = "",
    val category: String = "General",
    val priority: Priority = Priority.MEDIUM,
    val date: String, // YYYY-MM-DD
    val status: TaskStatus = TaskStatus.PENDING,
    val completedTimestamp: Long? = null,
    val notes: String = "",
    val estimatedDurationMinutes: Int = 30,
    val actualDurationMinutes: Int = 0,
    val preferredTime: String? = null,
    val isOneTime: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val dueDate: String? = null, // YYYY-MM-DD
    val dueTime: String? = null, // HH:mm
    val priority: Priority = Priority.MEDIUM,
    val category: String = "General",
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.IN_PROGRESS,
    val priority: Priority = Priority.MEDIUM,
    val startDate: String? = null,
    val deadline: String? = null,
    val technologies: String = "", // Comma-separated or tag list
    val notes: String = "",
    val links: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "project_tasks")
data class ProjectTaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ideas")
data class IdeaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val tags: String = "",
    val status: IdeaStatus = IdeaStatus.NEW,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "knowledge_items")
data class KnowledgeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val url: String? = null,
    val category: String = "Programming",
    val tags: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String = "General",
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val timeframe: GoalTimeframe = GoalTimeframe.WEEKLY,
    val targetDate: String? = null,
    val category: String = "General",
    val progressPercent: Int = 0,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val durationMinutes: Int,
    val date: String, // YYYY-MM-DD
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "credentials")
data class CredentialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val serviceName: String,
    val username: String,
    val maskedSecret: String,
    val url: String = "",
    val category: String = "Websites",
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "duas")
data class DuaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val arabicText: String,
    val translation: String,
    val reference: String,
    val category: String,
    val isFavorite: Boolean = false
)
