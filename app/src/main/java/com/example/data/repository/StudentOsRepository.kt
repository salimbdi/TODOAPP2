package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.engine.TaskEngine
import com.example.data.model.CredentialEntity
import com.example.data.model.DuaEntity
import com.example.data.model.GoalEntity
import com.example.data.model.GoalTimeframe
import com.example.data.model.IdeaEntity
import com.example.data.model.IdeaStatus
import com.example.data.model.KnowledgeEntity
import com.example.data.model.NoteEntity
import com.example.data.model.Priority
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectStatus
import com.example.data.model.ProjectTaskEntity
import com.example.data.model.RecurrenceType
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.TaskOccurrenceEntity
import com.example.data.model.TaskStatus
import com.example.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class StudentOsRepository(private val database: AppDatabase) {

    val taskDao = database.taskDao()
    val reminderDao = database.reminderDao()
    val projectDao = database.projectDao()
    val ideaDao = database.ideaDao()
    val knowledgeDao = database.knowledgeDao()
    val noteDao = database.noteDao()
    val goalDao = database.goalDao()
    val studyDao = database.studyDao()
    val credentialDao = database.credentialDao()
    val duaDao = database.duaDao()

    val taskEngine = TaskEngine(taskDao)

    // Flow streams
    fun getOccurrencesForDate(date: String): Flow<List<TaskOccurrenceEntity>> {
        return taskDao.getOccurrencesForDate(date)
    }

    val allRecurringTasks: Flow<List<RecurringTaskEntity>> = taskDao.getAllRecurringTasks()
    val allReminders: Flow<List<ReminderEntity>> = reminderDao.getAllReminders()
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
    val allProjectTasks: Flow<List<ProjectTaskEntity>> = projectDao.getAllProjectTasks()
    val allIdeas: Flow<List<IdeaEntity>> = ideaDao.getAllIdeas()
    val allKnowledge: Flow<List<KnowledgeEntity>> = knowledgeDao.getAllKnowledge()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allGoals: Flow<List<GoalEntity>> = goalDao.getAllGoals()
    val allStudySessions: Flow<List<StudySessionEntity>> = studyDao.getAllSessions()
    val allCredentials: Flow<List<CredentialEntity>> = credentialDao.getAllCredentials()
    val allDuas: Flow<List<DuaEntity>> = duaDao.getAllDuas()

    fun searchKnowledge(query: String): Flow<List<KnowledgeEntity>> = knowledgeDao.searchKnowledge(query)

    fun getProjectTasks(projectId: Long): Flow<List<ProjectTaskEntity>> = projectDao.getTasksForProject(projectId)

    /**
     * Ensures occurrences for the given date are generated idempotently.
     */
    suspend fun prepareDate(date: String) = withContext(Dispatchers.IO) {
        taskEngine.generateDailyOccurrences(date)
    }

    suspend fun setOccurrenceStatus(id: Long, status: TaskStatus) = withContext(Dispatchers.IO) {
        val timestamp = if (status == TaskStatus.COMPLETED) System.currentTimeMillis() else null
        taskDao.updateOccurrenceStatus(id, status, timestamp)
    }

    suspend fun addOneTimeTask(
        title: String,
        description: String = "",
        category: String = "General",
        priority: Priority = Priority.MEDIUM,
        date: String = DateUtils.today(),
        preferredTime: String? = null,
        durationMinutes: Int = 30
    ) = withContext(Dispatchers.IO) {
        val occurrence = TaskOccurrenceEntity(
            recurringTaskId = null,
            title = title,
            description = description,
            category = category,
            priority = priority,
            date = date,
            status = TaskStatus.PENDING,
            preferredTime = preferredTime,
            estimatedDurationMinutes = durationMinutes,
            isOneTime = true
        )
        taskDao.insertOccurrence(occurrence)
    }

    suspend fun addRecurringTask(
        title: String,
        description: String = "",
        category: String = "General",
        priority: Priority = Priority.MEDIUM,
        recurrenceType: RecurrenceType = RecurrenceType.DAILY,
        recurrenceDays: String = "1,2,3,4,5,6,7",
        startDate: String = DateUtils.today(),
        endDate: String? = null,
        preferredTime: String? = null,
        durationMinutes: Int = 30
    ) = withContext(Dispatchers.IO) {
        val recurring = RecurringTaskEntity(
            title = title,
            description = description,
            category = category,
            priority = priority,
            recurrenceType = recurrenceType,
            recurrenceDays = recurrenceDays,
            startDate = startDate,
            endDate = endDate,
            preferredTime = preferredTime,
            estimatedDurationMinutes = durationMinutes,
            isActive = true
        )
        val id = taskDao.insertRecurringTask(recurring)
        // Immediately generate occurrence for today if matches
        taskEngine.generateDailyOccurrences(DateUtils.today())
        id
    }

    suspend fun deleteOccurrence(id: Long) = withContext(Dispatchers.IO) {
        taskDao.deleteOccurrence(id)
    }

    suspend fun carryOverTask(occurrence: TaskOccurrenceEntity, targetDate: String) = withContext(Dispatchers.IO) {
        if (occurrence.isOneTime) {
            // Move one-time task to target date
            val updated = occurrence.copy(date = targetDate, status = TaskStatus.PENDING)
            taskDao.updateOccurrence(updated)
        } else {
            // For recurring task, if user explicitly wants to duplicate to another date as one-time
            val newTask = TaskOccurrenceEntity(
                recurringTaskId = null,
                title = occurrence.title + " (Carried over)",
                description = occurrence.description,
                category = occurrence.category,
                priority = occurrence.priority,
                date = targetDate,
                status = TaskStatus.PENDING,
                estimatedDurationMinutes = occurrence.estimatedDurationMinutes,
                isOneTime = true
            )
            taskDao.insertOccurrence(newTask)
        }
    }

    // Reminders
    suspend fun addReminder(reminder: ReminderEntity) = withContext(Dispatchers.IO) {
        reminderDao.insertReminder(reminder)
    }

    suspend fun toggleReminder(id: Long, isCompleted: Boolean) = withContext(Dispatchers.IO) {
        reminderDao.toggleReminderStatus(id, isCompleted)
    }

    suspend fun deleteReminder(id: Long) = withContext(Dispatchers.IO) {
        reminderDao.deleteReminder(id)
    }

    // Projects
    suspend fun addProject(project: ProjectEntity, initialTasks: List<String> = emptyList()) = withContext(Dispatchers.IO) {
        val projectId = projectDao.insertProject(project)
        for (taskTitle in initialTasks) {
            if (taskTitle.isNotBlank()) {
                projectDao.insertProjectTask(
                    ProjectTaskEntity(
                        projectId = projectId,
                        title = taskTitle.trim(),
                        isCompleted = false
                    )
                )
            }
        }
        projectId
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project)
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteTasksForProject(id)
        projectDao.deleteProject(id)
    }

    suspend fun addProjectTask(projectId: Long, title: String) = withContext(Dispatchers.IO) {
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = projectId, title = title))
    }

    suspend fun toggleProjectTask(task: ProjectTaskEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProjectTask(task.copy(isCompleted = !task.isCompleted))
    }

    suspend fun deleteProjectTask(id: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectTask(id)
    }

    // Ideas
    suspend fun addIdea(idea: IdeaEntity) = withContext(Dispatchers.IO) {
        ideaDao.insertIdea(idea)
    }

    suspend fun updateIdea(idea: IdeaEntity) = withContext(Dispatchers.IO) {
        ideaDao.updateIdea(idea)
    }

    suspend fun deleteIdea(id: Long) = withContext(Dispatchers.IO) {
        ideaDao.deleteIdea(id)
    }

    suspend fun convertIdeaToProject(idea: IdeaEntity) = withContext(Dispatchers.IO) {
        val project = ProjectEntity(
            name = idea.title,
            description = idea.description,
            status = ProjectStatus.PLANNED,
            priority = Priority.MEDIUM,
            technologies = idea.tags
        )
        val projId = projectDao.insertProject(project)
        ideaDao.updateIdea(idea.copy(status = IdeaStatus.CONVERTED_TO_PROJECT))
        projId
    }

    // Knowledge
    suspend fun addKnowledge(item: KnowledgeEntity) = withContext(Dispatchers.IO) {
        knowledgeDao.insertKnowledge(item)
    }

    suspend fun updateKnowledge(item: KnowledgeEntity) = withContext(Dispatchers.IO) {
        knowledgeDao.updateKnowledge(item)
    }

    suspend fun deleteKnowledge(id: Long) = withContext(Dispatchers.IO) {
        knowledgeDao.deleteKnowledge(id)
    }

    // Notes
    suspend fun addNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.insertNote(note)
    }

    suspend fun updateNote(note: NoteEntity) = withContext(Dispatchers.IO) {
        noteDao.updateNote(note.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteNote(id: Long) = withContext(Dispatchers.IO) {
        noteDao.deleteNote(id)
    }

    // Goals
    suspend fun addGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        goalDao.insertGoal(goal)
    }

    suspend fun updateGoal(goal: GoalEntity) = withContext(Dispatchers.IO) {
        goalDao.updateGoal(goal)
    }

    suspend fun deleteGoal(id: Long) = withContext(Dispatchers.IO) {
        goalDao.deleteGoal(id)
    }

    // Study sessions
    suspend fun logStudySession(subject: String, durationMinutes: Int, notes: String = "", date: String = DateUtils.today()) = withContext(Dispatchers.IO) {
        studyDao.insertSession(
            StudySessionEntity(
                subject = subject,
                durationMinutes = durationMinutes,
                date = date,
                notes = notes
            )
        )
    }

    suspend fun deleteStudySession(id: Long) = withContext(Dispatchers.IO) {
        studyDao.deleteSession(id)
    }

    // Credentials
    suspend fun addCredential(cred: CredentialEntity) = withContext(Dispatchers.IO) {
        credentialDao.insertCredential(cred)
    }

    suspend fun updateCredential(cred: CredentialEntity) = withContext(Dispatchers.IO) {
        credentialDao.updateCredential(cred.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteCredential(id: Long) = withContext(Dispatchers.IO) {
        credentialDao.deleteCredential(id)
    }

    // Duas
    suspend fun toggleDuaFavorite(id: Long, currentFav: Boolean) = withContext(Dispatchers.IO) {
        duaDao.toggleFavorite(id, !currentFav)
    }

    /**
     * Seeds initial demo data if database is empty.
     */
    suspend fun checkAndSeedInitialData() = withContext(Dispatchers.IO) {
        val existingRecurring = taskDao.getActiveRecurringTasks()
        if (existingRecurring.isNotEmpty()) {
            // Already seeded, just ensure today has occurrences
            taskEngine.generateDailyOccurrences(DateUtils.today())
            return@withContext
        }

        val today = DateUtils.today()

        // 1. Recurring tasks
        val rTasks = listOf(
            RecurringTaskEntity(
                title = "Study Artificial Intelligence & Deep Learning",
                description = "Deep dive into Transformer architectures, attention mechanisms, and PyTorch implementations",
                category = "AI",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.DAILY,
                startDate = today,
                preferredTime = "09:00",
                estimatedDurationMinutes = 60
            ),
            RecurringTaskEntity(
                title = "Solve LeetCode / Data Structures Problem",
                description = "Focus on dynamic programming, graph traversal, and trees",
                category = "Programming",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.DAILY,
                startDate = today,
                preferredTime = "11:30",
                estimatedDurationMinutes = 45
            ),
            RecurringTaskEntity(
                title = "Backend Architecture & API Development",
                description = "FastAPI endpoints, async queries, and database optimization",
                category = "Backend",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.WEEKDAYS,
                startDate = today,
                preferredTime = "14:00",
                estimatedDurationMinutes = 90
            ),
            RecurringTaskEntity(
                title = "Read 20 Pages of System Design / Tech Book",
                description = "Designing Data-Intensive Applications by Martin Kleppmann",
                category = "Learning",
                priority = Priority.LOW,
                recurrenceType = RecurrenceType.DAILY,
                startDate = today,
                preferredTime = "21:30",
                estimatedDurationMinutes = 30
            ),
            RecurringTaskEntity(
                title = "Physical Exercise & Cardio",
                description = "Gym workout or 5km run to maintain student health & energy",
                category = "Health",
                priority = Priority.MEDIUM,
                recurrenceType = RecurrenceType.DAILY,
                startDate = today,
                preferredTime = "17:30",
                estimatedDurationMinutes = 60
            ),
            RecurringTaskEntity(
                title = "Morning Adhkar & Quran Routine",
                description = "Spiritual grounding, peace, and focus before beginning academic work",
                category = "Religious",
                priority = Priority.HIGH,
                recurrenceType = RecurrenceType.DAILY,
                startDate = today,
                preferredTime = "06:00",
                estimatedDurationMinutes = 20
            )
        )

        for (rt in rTasks) {
            taskDao.insertRecurringTask(rt)
        }

        // Generate occurrences for today
        taskEngine.generateDailyOccurrences(today)

        // Add 2 one-time tasks for today / upcoming
        taskDao.insertOccurrence(
            TaskOccurrenceEntity(
                recurringTaskId = null,
                title = "Fix JWT Refresh Token Bug in Auth Service",
                description = "Handle expired token edge-case in Axios / Retrofit interceptor",
                category = "Backend",
                priority = Priority.HIGH,
                date = today,
                status = TaskStatus.PENDING,
                preferredTime = "16:00",
                estimatedDurationMinutes = 45,
                isOneTime = true
            )
        )

        taskDao.insertOccurrence(
            TaskOccurrenceEntity(
                recurringTaskId = null,
                title = "Submit Distributed Systems Lab Report 2",
                description = "Include Raft consensus benchmark results and diagrams",
                category = "University",
                priority = Priority.URGENT,
                date = DateUtils.addDays(today, 2),
                status = TaskStatus.PENDING,
                preferredTime = "23:59",
                estimatedDurationMinutes = 120,
                isOneTime = true
            )
        )

        // 2. Reminders
        reminderDao.insertReminder(
            ReminderEntity(
                title = "Renew University Library books",
                description = "Operating Systems Concepts (Silberschatz)",
                dueDate = DateUtils.addDays(today, 1),
                priority = Priority.MEDIUM,
                category = "University"
            )
        )
        reminderDao.insertReminder(
            ReminderEntity(
                title = "Check Algerian AI Student Research Fellowship portal",
                description = "Verify submission of CV and recommendation letter",
                dueDate = DateUtils.addDays(today, 4),
                priority = Priority.HIGH,
                category = "University"
            )
        )
        reminderDao.insertReminder(
            ReminderEntity(
                title = "Renew AWS Educate Student Cloud Credits",
                description = "Ensure GPU test instance credits are refreshed",
                dueDate = null,
                priority = Priority.LOW,
                category = "Cloud"
            )
        )

        // 3. Projects
        val p1Id = projectDao.insertProject(
            ProjectEntity(
                name = "AI Medical Diagnosis Assistant",
                description = "Clinical QA chatbot using RAG, LlamaIndex, and medical literature embeddings",
                status = ProjectStatus.IN_PROGRESS,
                priority = Priority.HIGH,
                startDate = today,
                deadline = DateUtils.addDays(today, 30),
                technologies = "Python, FastAPI, RAG, Supabase, pgvector"
            )
        )
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p1Id, title = "Design database schema & vector index", isCompleted = true))
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p1Id, title = "Build FastAPI inference endpoint", isCompleted = true))
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p1Id, title = "Integrate RAG retrieval pipeline with citations", isCompleted = false))
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p1Id, title = "Benchmark latency and hallucination rate", isCompleted = false))

        val p2Id = projectDao.insertProject(
            ProjectEntity(
                name = "Distributed Microservices Architecture",
                description = "Event-driven architecture with Kafka messaging and CQRS pattern",
                status = ProjectStatus.PLANNED,
                priority = Priority.MEDIUM,
                startDate = today,
                deadline = DateUtils.addDays(today, 45),
                technologies = "Kotlin, Spring Boot, Kafka, PostgreSQL, Docker"
            )
        )
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p2Id, title = "Setup Docker Compose Kafka cluster", isCompleted = true))
        projectDao.insertProjectTask(ProjectTaskEntity(projectId = p2Id, title = "Implement idempotency key middleware", isCompleted = false))

        // 4. Ideas
        ideaDao.insertIdea(
            IdeaEntity(
                title = "AI Academic Mentor for Algerian University Students",
                description = "Bilingual (Arabic/French/English) bot trained on Algerian national curriculum and university guides.",
                tags = "#AI #EdTech #University",
                status = IdeaStatus.DEVELOPING
            )
        )
        ideaDao.insertIdea(
            IdeaEntity(
                title = "Automated CLI Git Branch & PR Sync Tool",
                description = "CLI tool written in Go or Kotlin that matches commit messages with Jira/GitHub issues automatically.",
                tags = "#DevOps #Productivity",
                status = IdeaStatus.NEW
            )
        )

        // 5. Knowledge items
        knowledgeDao.insertKnowledge(
            KnowledgeEntity(
                title = "Self-Attention Mechanism in Transformers",
                content = "Attention(Q,K,V) = softmax(Q K^T / sqrt(d_k)) V. Each token computes an alignment score with all other tokens in sequence, overcoming RNN vanishing gradient and allowing full parallelization.",
                url = "https://arxiv.org/abs/1706.03762",
                category = "AI",
                tags = "#AI #Transformers #DeepLearning",
                notes = "Core reading: Attention Is All You Need"
            )
        )
        knowledgeDao.insertKnowledge(
            KnowledgeEntity(
                title = "PostgreSQL B-Tree vs GIN & pgvector Indexes",
                content = "B-Tree is optimal for scalar equality/range. GIN is essential for JSONB containment (@>) and inverted text index. For embedding vectors, HNSW index provides logarithmic-time nearest neighbor queries.",
                url = "https://github.com/pgvector/pgvector",
                category = "Backend",
                tags = "#Database #PostgreSQL #VectorDB",
                notes = "Use m=16, ef_construction=64 for balance between build time and recall."
            )
        )
        knowledgeDao.insertKnowledge(
            KnowledgeEntity(
                title = "Clean Architecture in Jetpack Compose & Kotlin",
                content = "Strict unidirection flow: UI -> ViewModel (exposing StateFlow) -> Repository -> Room Local / Retrofit Remote. Keeps business logic cleanly decoupled and testable via JVM Robolectric tests.",
                url = "https://developer.android.com/topic/architecture",
                category = "Programming",
                tags = "#Android #Kotlin #CleanArchitecture"
            )
        )

        // 6. Notes
        noteDao.insertNote(
            NoteEntity(
                title = "CS402: LL(1) Parsing Table Construction",
                content = "Steps: 1. Calculate FIRST sets for all non-terminals. 2. Calculate FOLLOW sets for non-terminals. 3. Fill parse table: for A -> alpha, for each terminal a in FIRST(alpha), M[A, a] = A -> alpha. Check for conflicts.",
                category = "University",
                tags = "#Compilers #LectureNotes"
            )
        )
        noteDao.insertNote(
            NoteEntity(
                title = "FastAPI Concurrency vs AsyncIO Event Loop",
                content = "Remember: `def` endpoints run in an external threadpool (so blocking I/O is safe). `async def` endpoints run on the main event loop, so NEVER use blocking time.sleep() or sync DB drivers inside `async def`.",
                category = "Backend",
                tags = "#Python #FastAPI #Concurrency"
            )
        )

        // 7. Goals
        goalDao.insertGoal(
            GoalEntity(
                title = "Master Modern AI & Production LLM Engineering",
                description = "Build deep competency in Transformers, RAG pipelines, fine-tuning, and model evaluation",
                timeframe = GoalTimeframe.YEARLY,
                targetDate = "2026-12-31",
                category = "AI",
                progressPercent = 55
            )
        )
        goalDao.insertGoal(
            GoalEntity(
                title = "Deliver Medical RAG Assistant MVP",
                description = "Complete core retrieval pipeline and test with medical sample query set",
                timeframe = GoalTimeframe.MONTHLY,
                targetDate = DateUtils.addDays(today, 25),
                category = "Projects",
                progressPercent = 65
            )
        )
        goalDao.insertGoal(
            GoalEntity(
                title = "Complete 5 LeetCode DP Problems This Week",
                description = "Target coin change, longest common subsequence, and knapsack",
                timeframe = GoalTimeframe.WEEKLY,
                category = "Programming",
                progressPercent = 60
            )
        )
        goalDao.insertGoal(
            GoalEntity(
                title = "Daily 90-min focused study block",
                description = "Zero distraction deep work session",
                timeframe = GoalTimeframe.DAILY,
                category = "Productivity",
                progressPercent = 50
            )
        )

        // 8. Study sessions
        studyDao.insertSession(
            StudySessionEntity(
                subject = "AI & Transformer Models",
                durationMinutes = 90,
                date = today,
                notes = "Studied FlashAttention-2 speedups and key-value cache memory footprints."
            )
        )
        studyDao.insertSession(
            StudySessionEntity(
                subject = "FastAPI & PostgreSQL",
                durationMinutes = 60,
                date = today,
                notes = "Optimized connection pooling and prepared statements."
            )
        )
        studyDao.insertSession(
            StudySessionEntity(
                subject = "Data Structures & DP",
                durationMinutes = 45,
                date = DateUtils.addDays(today, -1),
                notes = "Solved unbounded knapsack problem variants."
            )
        )

        // 9. Duas & Adhkar
        val authenticDuas = listOf(
            DuaEntity(
                arabicText = "رَّبِّ زِدْنِي عِلْمًا",
                translation = "My Lord, increase me in knowledge.",
                reference = "Surah Ta-Ha (20:114)",
                category = "Knowledge",
                isFavorite = true
            ),
            DuaEntity(
                arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلاً مُتَقَبَّلاً",
                translation = "O Allah, I ask You for beneficial knowledge, goodly provision, and acceptable deeds.",
                reference = "Sunan Ibn Majah 925 (Sahih)",
                category = "Morning",
                isFavorite = true
            ),
            DuaEntity(
                arabicText = "اللَّهُمَّ لَا سَهْلَ إِلَّا مَا جَعَلْتَهُ سَهْلاً، وَأَنْتَ تَجْعَلُ الحَزْنَ إِذَا شِئْتَ سَهْلاً",
                translation = "O Allah, there is nothing easy except that which You make easy, and You make hardship, if You will, easy.",
                reference = "Sahih Ibn Hibban 974",
                category = "Success & Exams",
                isFavorite = true
            ),
            DuaEntity(
                arabicText = "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنَ الْهَمِّ وَالْحَزَنِ، وَالْعَجْزِ وَالْكَسَلِ، وَالْبُخْلِ وَالْجُبْنِ، وَضَلَعِ الدَّيْنِ، وَغَلَبَةِ الرِّجَالِ",
                translation = "O Allah, I seek refuge in You from anxiety and sorrow, weakness and laziness, miserliness and cowardice, the burden of debt and from being overpowered by men.",
                reference = "Sahih Al-Bukhari 2893",
                category = "Protection & Energy",
                isFavorite = true
            ),
            DuaEntity(
                arabicText = "حَسْبِيَ اللَّهُ لَا إِلَهَ إِلَّا هُوَ عَلَيْهِ تَوَكَّلْتُ وَهُوَ رَبُّ الْعَرْشِ الْعَظِيمِ",
                translation = "Sufficient for me is Allah; there is no deity except Him. On Him I have relied, and He is the Lord of the Great Throne.",
                reference = "Sunan Abi Dawud 5081 (Sahih)",
                category = "Morning & Evening",
                isFavorite = false
            ),
            DuaEntity(
                arabicText = "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْعَفْوَ وَالْعَافِيَةَ فِي الدُّنْيَا وَالْآخِرَةِ",
                translation = "O Allah, I ask You for pardon and well-being in this world and in the Hereafter.",
                reference = "Sunan Abi Dawud 5074 (Sahih)",
                category = "Daily Life",
                isFavorite = false
            )
        )
        duaDao.insertDuas(authenticDuas)

        // 10. Credentials
        credentialDao.insertCredential(
            CredentialEntity(
                serviceName = "GitHub (Student Dev Pack)",
                username = "s-bdirina",
                maskedSecret = "ghp_kL82jK82M1nO928PqRsTuV9912",
                url = "https://github.com",
                category = "Development",
                notes = "Personal student developer account with CoPilot and Cloud benefits."
            )
        )
        credentialDao.insertCredential(
            CredentialEntity(
                serviceName = "University Portal (ESTIN)",
                username = "s_bdirina@estin.dz",
                maskedSecret = "Estin#Stud2026!Secure",
                url = "https://ent.estin.dz",
                category = "University",
                notes = "Course schedules, grades, and academic submissions."
            )
        )
        credentialDao.insertCredential(
            CredentialEntity(
                serviceName = "HuggingFace API Token",
                username = "student_ai_lab",
                maskedSecret = "hf_xZ98qW12eR34tY56uI78oP90",
                url = "https://huggingface.co",
                category = "AI",
                notes = "Model weights download and serverless inference endpoint."
            )
        )
    }
}
