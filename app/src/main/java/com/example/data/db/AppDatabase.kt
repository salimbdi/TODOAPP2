package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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

@Database(
    entities = [
        RecurringTaskEntity::class,
        TaskOccurrenceEntity::class,
        ReminderEntity::class,
        ProjectEntity::class,
        ProjectTaskEntity::class,
        IdeaEntity::class,
        KnowledgeEntity::class,
        NoteEntity::class,
        GoalEntity::class,
        StudySessionEntity::class,
        CredentialEntity::class,
        DuaEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun projectDao(): ProjectDao
    abstract fun ideaDao(): IdeaDao
    abstract fun knowledgeDao(): KnowledgeDao
    abstract fun noteDao(): NoteDao
    abstract fun goalDao(): GoalDao
    abstract fun studyDao(): StudyDao
    abstract fun credentialDao(): CredentialDao
    abstract fun duaDao(): DuaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "student_os_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
