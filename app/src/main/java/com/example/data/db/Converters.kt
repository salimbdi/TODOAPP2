package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.GoalTimeframe
import com.example.data.model.IdeaStatus
import com.example.data.model.Priority
import com.example.data.model.ProjectStatus
import com.example.data.model.RecurrenceType
import com.example.data.model.TaskStatus

class Converters {
    @TypeConverter
    fun fromPriority(value: Priority?): String = value?.name ?: Priority.MEDIUM.name

    @TypeConverter
    fun toPriority(value: String?): Priority =
        try { Priority.valueOf(value ?: Priority.MEDIUM.name) } catch (_: Exception) { Priority.MEDIUM }

    @TypeConverter
    fun fromRecurrenceType(value: RecurrenceType?): String = value?.name ?: RecurrenceType.DAILY.name

    @TypeConverter
    fun toRecurrenceType(value: String?): RecurrenceType =
        try { RecurrenceType.valueOf(value ?: RecurrenceType.DAILY.name) } catch (_: Exception) { RecurrenceType.DAILY }

    @TypeConverter
    fun fromTaskStatus(value: TaskStatus?): String = value?.name ?: TaskStatus.PENDING.name

    @TypeConverter
    fun toTaskStatus(value: String?): TaskStatus =
        try { TaskStatus.valueOf(value ?: TaskStatus.PENDING.name) } catch (_: Exception) { TaskStatus.PENDING }

    @TypeConverter
    fun fromProjectStatus(value: ProjectStatus?): String = value?.name ?: ProjectStatus.IN_PROGRESS.name

    @TypeConverter
    fun toProjectStatus(value: String?): ProjectStatus =
        try { ProjectStatus.valueOf(value ?: ProjectStatus.IN_PROGRESS.name) } catch (_: Exception) { ProjectStatus.IN_PROGRESS }

    @TypeConverter
    fun fromIdeaStatus(value: IdeaStatus?): String = value?.name ?: IdeaStatus.NEW.name

    @TypeConverter
    fun toIdeaStatus(value: String?): IdeaStatus =
        try { IdeaStatus.valueOf(value ?: IdeaStatus.NEW.name) } catch (_: Exception) { IdeaStatus.NEW }

    @TypeConverter
    fun fromGoalTimeframe(value: GoalTimeframe?): String = value?.name ?: GoalTimeframe.WEEKLY.name

    @TypeConverter
    fun toGoalTimeframe(value: String?): GoalTimeframe =
        try { GoalTimeframe.valueOf(value ?: GoalTimeframe.WEEKLY.name) } catch (_: Exception) { GoalTimeframe.WEEKLY }
}
