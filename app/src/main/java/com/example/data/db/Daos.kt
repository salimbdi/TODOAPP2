package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CredentialEntity
import com.example.data.model.DuaEntity
import com.example.data.model.GoalEntity
import com.example.data.model.IdeaEntity
import com.example.data.model.KnowledgeEntity
import com.example.data.model.NoteEntity
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectTaskEntity
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.TaskOccurrenceEntity
import com.example.data.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM recurring_tasks ORDER BY createdAt DESC")
    fun getAllRecurringTasks(): Flow<List<RecurringTaskEntity>>

    @Query("SELECT * FROM recurring_tasks WHERE isActive = 1")
    suspend fun getActiveRecurringTasks(): List<RecurringTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringTask(task: RecurringTaskEntity): Long

    @Update
    suspend fun updateRecurringTask(task: RecurringTaskEntity)

    @Query("DELETE FROM recurring_tasks WHERE id = :id")
    suspend fun deleteRecurringTask(id: Long)

    @Query("SELECT * FROM task_occurrences WHERE date = :date ORDER BY orderIndex ASC, id ASC")
    fun getOccurrencesForDate(date: String): Flow<List<TaskOccurrenceEntity>>

    @Query("SELECT * FROM task_occurrences WHERE date = :date")
    suspend fun getOccurrencesForDateSync(date: String): List<TaskOccurrenceEntity>

    @Query("SELECT * FROM task_occurrences WHERE recurringTaskId = :recurringTaskId AND date = :date LIMIT 1")
    suspend fun getOccurrenceForRecurringTaskAndDate(recurringTaskId: Long, date: String): TaskOccurrenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: TaskOccurrenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrences(occurrences: List<TaskOccurrenceEntity>)

    @Update
    suspend fun updateOccurrence(occurrence: TaskOccurrenceEntity)

    @Query("UPDATE task_occurrences SET status = :status, completedTimestamp = :timestamp WHERE id = :id")
    suspend fun updateOccurrenceStatus(id: Long, status: TaskStatus, timestamp: Long?)

    @Query("DELETE FROM task_occurrences WHERE id = :id")
    suspend fun deleteOccurrence(id: Long)

    @Query("SELECT * FROM task_occurrences WHERE date BETWEEN :startDate AND :endDate")
    fun getOccurrencesBetweenDates(startDate: String, endDate: String): Flow<List<TaskOccurrenceEntity>>

    @Query("SELECT * FROM task_occurrences ORDER BY date DESC, id DESC")
    fun getAllOccurrences(): Flow<List<TaskOccurrenceEntity>>
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY isCompleted ASC, dueDate ASC, id DESC")
    fun getAllReminders(): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun toggleReminderStatus(id: Long, isCompleted: Boolean)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteReminder(id: Long)
}

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM projects WHERE id = :id")
    suspend fun deleteProject(id: Long)

    @Query("SELECT * FROM project_tasks WHERE projectId = :projectId ORDER BY id ASC")
    fun getTasksForProject(projectId: Long): Flow<List<ProjectTaskEntity>>

    @Query("SELECT * FROM project_tasks")
    fun getAllProjectTasks(): Flow<List<ProjectTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProjectTask(task: ProjectTaskEntity): Long

    @Update
    suspend fun updateProjectTask(task: ProjectTaskEntity)

    @Query("DELETE FROM project_tasks WHERE id = :id")
    suspend fun deleteProjectTask(id: Long)

    @Query("DELETE FROM project_tasks WHERE projectId = :projectId")
    suspend fun deleteTasksForProject(projectId: Long)
}

@Dao
interface IdeaDao {
    @Query("SELECT * FROM ideas ORDER BY createdAt DESC")
    fun getAllIdeas(): Flow<List<IdeaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIdea(idea: IdeaEntity): Long

    @Update
    suspend fun updateIdea(idea: IdeaEntity)

    @Query("DELETE FROM ideas WHERE id = :id")
    suspend fun deleteIdea(id: Long)
}

@Dao
interface KnowledgeDao {
    @Query("SELECT * FROM knowledge_items ORDER BY createdAt DESC")
    fun getAllKnowledge(): Flow<List<KnowledgeEntity>>

    @Query("SELECT * FROM knowledge_items WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchKnowledge(query: String): Flow<List<KnowledgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKnowledge(item: KnowledgeEntity): Long

    @Update
    suspend fun updateKnowledge(item: KnowledgeEntity)

    @Query("DELETE FROM knowledge_items WHERE id = :id")
    suspend fun deleteKnowledge(id: Long)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)
}

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY isCompleted ASC, createdAt DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteGoal(id: Long)
}

@Dao
interface StudyDao {
    @Query("SELECT * FROM study_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<StudySessionEntity>>

    @Query("SELECT * FROM study_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsForDate(date: String): Flow<List<StudySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: StudySessionEntity): Long

    @Query("DELETE FROM study_sessions WHERE id = :id")
    suspend fun deleteSession(id: Long)
}

@Dao
interface CredentialDao {
    @Query("SELECT * FROM credentials ORDER BY serviceName ASC")
    fun getAllCredentials(): Flow<List<CredentialEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCredential(cred: CredentialEntity): Long

    @Update
    suspend fun updateCredential(cred: CredentialEntity)

    @Query("DELETE FROM credentials WHERE id = :id")
    suspend fun deleteCredential(id: Long)
}

@Dao
interface DuaDao {
    @Query("SELECT * FROM duas ORDER BY id ASC")
    fun getAllDuas(): Flow<List<DuaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDuas(duas: List<DuaEntity>)

    @Query("UPDATE duas SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)
}
