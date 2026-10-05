package com.example.data.model

enum class Priority {
    LOW,
    MEDIUM,
    HIGH,
    URGENT;

    val displayName: String
        get() = when (this) {
            LOW -> "Low"
            MEDIUM -> "Medium"
            HIGH -> "High"
            URGENT -> "Urgent"
        }
}

enum class RecurrenceType {
    DAILY,
    WEEKDAYS,
    WEEKENDS,
    SPECIFIC_DAYS,
    WEEKLY,
    MONTHLY;

    val displayName: String
        get() = when (this) {
            DAILY -> "Every Day"
            WEEKDAYS -> "Weekdays (Mon-Fri)"
            WEEKENDS -> "Weekends (Sat-Sun)"
            SPECIFIC_DAYS -> "Specific Days"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
        }
}

enum class TaskStatus {
    PENDING,
    COMPLETED,
    MISSED,
    CANCELLED
}

enum class ProjectStatus {
    IDEA,
    PLANNED,
    IN_PROGRESS,
    PAUSED,
    COMPLETED,
    ARCHIVED;

    val displayName: String
        get() = when (this) {
            IDEA -> "Idea"
            PLANNED -> "Planned"
            IN_PROGRESS -> "In Progress"
            PAUSED -> "Paused"
            COMPLETED -> "Completed"
            ARCHIVED -> "Archived"
        }
}

enum class IdeaStatus {
    NEW,
    INTERESTING,
    DEVELOPING,
    CONVERTED_TO_PROJECT,
    ARCHIVED;

    val displayName: String
        get() = when (this) {
            NEW -> "New"
            INTERESTING -> "Interesting"
            DEVELOPING -> "Developing"
            CONVERTED_TO_PROJECT -> "Converted"
            ARCHIVED -> "Archived"
        }
}

enum class GoalTimeframe {
    DAILY,
    WEEKLY,
    MONTHLY,
    YEARLY;

    val displayName: String
        get() = when (this) {
            DAILY -> "Daily"
            WEEKLY -> "Weekly"
            MONTHLY -> "Monthly"
            YEARLY -> "Yearly"
        }
}
