package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.engine.TaskEngine
import com.example.data.model.Priority
import com.example.data.model.RecurrenceType
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.TaskStatus
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Requirement 53:
 * Scenario:
 * Create: "Study AI", Repeat: Every day
 * Simulate:
 * October 5: Pending -> Complete
 * October 6: Must appear as Pending -> Leave incomplete
 * October 7: Must appear as Pending -> Complete
 * Then inspect history:
 * October 5 -> Completed
 * October 6 -> Incomplete
 * October 7 -> Completed
 * Verify:
 * 1. Three separate occurrences.
 * 2. No duplicate occurrences.
 * 3. No incorrect completion propagation.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TaskEngineScenarioTest {

    private lateinit var db: AppDatabase
    private lateinit var taskEngine: TaskEngine

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        taskEngine = TaskEngine(db.taskDao())
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `test recurring task three-day independent occurrences and history preservation`() = runBlocking {
        val taskDao = db.taskDao()

        // 1. Create recurring task: "Study AI"
        val recurringId = taskDao.insertRecurringTask(
            RecurringTaskEntity(
                title = "Study AI",
                description = "Daily deep work on AI models",
                category = "AI",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.DAILY,
                startDate = "2026-10-05"
            )
        )

        // 2. October 5 simulation:
        val oct5List1 = taskEngine.generateDailyOccurrences("2026-10-05")
        assertEquals(1, oct5List1.size)
        val oct5Task = oct5List1[0]
        assertEquals("Study AI", oct5Task.title)
        assertEquals("2026-10-05", oct5Task.date)
        assertEquals(TaskStatus.PENDING, oct5Task.status)

        // Test idempotency: calling generateDailyOccurrences again must NOT duplicate
        val oct5List2 = taskEngine.generateDailyOccurrences("2026-10-05")
        assertEquals(1, oct5List2.size)

        // Complete October 5 occurrence
        taskDao.updateOccurrenceStatus(oct5Task.id, TaskStatus.COMPLETED, 1728120000000L)

        // Verify October 5 is completed
        val oct5Updated = taskDao.getOccurrenceForRecurringTaskAndDate(recurringId, "2026-10-05")
        assertNotNull(oct5Updated)
        assertEquals(TaskStatus.COMPLETED, oct5Updated?.status)

        // 3. October 6 simulation:
        val oct6List = taskEngine.generateDailyOccurrences("2026-10-06")
        assertEquals(1, oct6List.size)
        val oct6Task = oct6List[0]
        assertEquals("Study AI", oct6Task.title)
        assertEquals("2026-10-06", oct6Task.date)
        // Must appear as Pending, NOT affected by October 5 completion!
        assertEquals(TaskStatus.PENDING, oct6Task.status)
        assertNotEquals(oct5Task.id, oct6Task.id)

        // Leave October 6 incomplete (Pending)

        // 4. October 7 simulation:
        val oct7List = taskEngine.generateDailyOccurrences("2026-10-07")
        assertEquals(1, oct7List.size)
        val oct7Task = oct7List[0]
        assertEquals("Study AI", oct7Task.title)
        assertEquals("2026-10-07", oct7Task.date)
        // Must appear as Pending
        assertEquals(TaskStatus.PENDING, oct7Task.status)
        assertNotEquals(oct6Task.id, oct7Task.id)

        // Complete October 7 occurrence
        taskDao.updateOccurrenceStatus(oct7Task.id, TaskStatus.COMPLETED, 1728292800000L)

        // 5. Inspect full history across all 3 days
        val oct5History = taskDao.getOccurrenceForRecurringTaskAndDate(recurringId, "2026-10-05")
        val oct6History = taskDao.getOccurrenceForRecurringTaskAndDate(recurringId, "2026-10-06")
        val oct7History = taskDao.getOccurrenceForRecurringTaskAndDate(recurringId, "2026-10-07")

        assertNotNull(oct5History)
        assertNotNull(oct6History)
        assertNotNull(oct7History)

        // Verified states:
        assertEquals(TaskStatus.COMPLETED, oct5History?.status)
        assertEquals(TaskStatus.PENDING, oct6History?.status) // Incomplete
        assertEquals(TaskStatus.COMPLETED, oct7History?.status)

        // Verified separate IDs:
        val ids = setOf(oct5History?.id, oct6History?.id, oct7History?.id)
        assertEquals(3, ids.size)

        // Re-run generation on all 3 dates to verify strict idempotency
        taskEngine.generateDailyOccurrences("2026-10-05")
        taskEngine.generateDailyOccurrences("2026-10-06")
        taskEngine.generateDailyOccurrences("2026-10-07")

        val totalOccurrences = taskDao.getOccurrencesForDateSync("2026-10-05").size +
                taskDao.getOccurrencesForDateSync("2026-10-06").size +
                taskDao.getOccurrencesForDateSync("2026-10-07").size

        assertEquals(3, totalOccurrences)
    }
}
