package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.CredentialEntity
import com.example.data.model.DuaEntity
import com.example.data.model.GoalEntity
import com.example.data.model.GoalTimeframe
import com.example.data.model.IdeaEntity
import com.example.data.model.KnowledgeEntity
import com.example.data.model.NoteEntity
import com.example.data.model.Priority
import com.example.data.model.ProjectEntity
import com.example.data.model.ProjectTaskEntity
import com.example.data.model.RecurrenceType
import com.example.data.model.RecurringTaskEntity
import com.example.data.model.ReminderEntity
import com.example.data.model.StudySessionEntity
import com.example.data.model.TaskOccurrenceEntity
import com.example.data.model.TaskStatus
import com.example.data.repository.StudentOsRepository
import com.example.util.DateUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class SearchResultItem(
    val id: Long,
    val type: String, // "Task", "Project", "Note", "Knowledge", "Reminder", "Idea"
    val title: String,
    val subtitle: String,
    val category: String = ""
)

class StudentOsViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    val repository = StudentOsRepository(database)

    private val _selectedDate = MutableStateFlow(DateUtils.today())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Active Study Timer state
    private val _isTimerRunning = MutableStateFlow(false)
    val isTimerRunning: StateFlow<Boolean> = _isTimerRunning.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0)
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _timerSubject = MutableStateFlow("AI & Machine Learning")
    val timerSubject: StateFlow<String> = _timerSubject.asStateFlow()

    private var timerJob: Job? = null

    // Reactive streams
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<List<TaskOccurrenceEntity>> = _selectedDate
        .flatMapLatest { date -> repository.getOccurrencesForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recurringTasks: StateFlow<List<RecurringTaskEntity>> = repository.allRecurringTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = repository.allReminders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projectTasks: StateFlow<List<ProjectTaskEntity>> = repository.allProjectTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val ideas: StateFlow<List<IdeaEntity>> = repository.allIdeas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val knowledgeItems: StateFlow<List<KnowledgeEntity>> = repository.allKnowledge
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val goals: StateFlow<List<GoalEntity>> = repository.allGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.allStudySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val credentials: StateFlow<List<CredentialEntity>> = repository.allCredentials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val duas: StateFlow<List<DuaEntity>> = repository.allDuas
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined global search results
    val searchResults: StateFlow<List<SearchResultItem>> = combine(
        _searchQuery,
        tasksForSelectedDate,
        projects,
        notes,
        knowledgeItems
    ) { query, tasks, projs, nts, know ->
        if (query.isBlank() || query.length < 2) return@combine emptyList()
        val q = query.trim().lowercase()
        val list = mutableListOf<SearchResultItem>()

        tasks.filter { it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
            .forEach { list.add(SearchResultItem(it.id, "Task", it.title, "Status: ${it.status.name} • ${it.category}", it.category)) }

        projs.filter { it.name.lowercase().contains(q) || it.technologies.lowercase().contains(q) }
            .forEach { list.add(SearchResultItem(it.id, "Project", it.name, "Tech: ${it.technologies}", "Project")) }

        nts.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) }
            .forEach { list.add(SearchResultItem(it.id, "Note", it.title, it.category, it.category)) }

        know.filter { it.title.lowercase().contains(q) || it.content.lowercase().contains(q) || it.tags.lowercase().contains(q) }
            .forEach { list.add(SearchResultItem(it.id, "Knowledge", it.title, it.category, it.category)) }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
            repository.prepareDate(_selectedDate.value)
        }
    }

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
        viewModelScope.launch {
            repository.prepareDate(date)
        }
    }

    fun goToToday() {
        setSelectedDate(DateUtils.today())
    }

    fun goToNextDay() {
        val next = DateUtils.addDays(_selectedDate.value, 1)
        setSelectedDate(next)
    }

    fun goToPrevDay() {
        val prev = DateUtils.addDays(_selectedDate.value, -1)
        setSelectedDate(prev)
    }

    fun toggleTaskCompletion(occurrence: TaskOccurrenceEntity) {
        viewModelScope.launch {
            val nextStatus = if (occurrence.status == TaskStatus.COMPLETED) {
                TaskStatus.PENDING
            } else {
                TaskStatus.COMPLETED
            }
            repository.setOccurrenceStatus(occurrence.id, nextStatus)
        }
    }

    fun carryOverTask(occurrence: TaskOccurrenceEntity, targetDate: String = DateUtils.today()) {
        viewModelScope.launch {
            repository.carryOverTask(occurrence, targetDate)
            _userMessage.value = "Task carried over to ${DateUtils.formatDisplay(targetDate)}"
        }
    }

    fun deleteTaskOccurrence(id: Long) {
        viewModelScope.launch {
            repository.deleteOccurrence(id)
            _userMessage.value = "Task removed"
        }
    }

    fun addOneTimeTask(
        title: String,
        description: String = "",
        category: String = "General",
        priority: Priority = Priority.MEDIUM,
        date: String = _selectedDate.value,
        preferredTime: String? = null,
        durationMinutes: Int = 30
    ) {
        viewModelScope.launch {
            repository.addOneTimeTask(
                title = title,
                description = description,
                category = category,
                priority = priority,
                date = date,
                preferredTime = preferredTime,
                durationMinutes = durationMinutes
            )
            _userMessage.value = "Task added for ${DateUtils.formatDisplay(date)}"
        }
    }

    fun addRecurringTask(
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
    ) {
        viewModelScope.launch {
            repository.addRecurringTask(
                title = title,
                description = description,
                category = category,
                priority = priority,
                recurrenceType = recurrenceType,
                recurrenceDays = recurrenceDays,
                startDate = startDate,
                endDate = endDate,
                preferredTime = preferredTime,
                durationMinutes = durationMinutes
            )
            repository.prepareDate(_selectedDate.value)
            _userMessage.value = "Recurring task created ($title)"
        }
    }

    // Reminders
    fun addReminder(
        title: String,
        description: String = "",
        dueDate: String? = null,
        dueTime: String? = null,
        priority: Priority = Priority.MEDIUM,
        category: String = "General"
    ) {
        viewModelScope.launch {
            repository.addReminder(
                ReminderEntity(
                    title = title,
                    description = description,
                    dueDate = dueDate,
                    dueTime = dueTime,
                    priority = priority,
                    category = category
                )
            )
            _userMessage.value = "Reminder set"
        }
    }

    fun toggleReminder(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, isCompleted)
        }
    }

    fun deleteReminder(id: Long) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    // Projects
    fun addProject(
        name: String,
        description: String,
        status: com.example.data.model.ProjectStatus,
        priority: Priority,
        technologies: String,
        deadline: String?,
        tasks: List<String>
    ) {
        viewModelScope.launch {
            repository.addProject(
                ProjectEntity(
                    name = name,
                    description = description,
                    status = status,
                    priority = priority,
                    startDate = DateUtils.today(),
                    deadline = deadline,
                    technologies = technologies
                ),
                initialTasks = tasks
            )
            _userMessage.value = "Project '$name' created"
        }
    }

    fun toggleProjectTask(task: ProjectTaskEntity) {
        viewModelScope.launch {
            repository.toggleProjectTask(task)
        }
    }

    fun addProjectTask(projectId: Long, title: String) {
        viewModelScope.launch {
            repository.addProjectTask(projectId, title)
        }
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch {
            repository.deleteProject(id)
            _userMessage.value = "Project deleted"
        }
    }

    // Ideas
    fun addIdea(title: String, description: String, tags: String) {
        viewModelScope.launch {
            repository.addIdea(
                IdeaEntity(
                    title = title,
                    description = description,
                    tags = tags
                )
            )
            _userMessage.value = "Idea captured"
        }
    }

    fun convertIdeaToProject(idea: IdeaEntity) {
        viewModelScope.launch {
            repository.convertIdeaToProject(idea)
            _userMessage.value = "Converted idea into Project!"
        }
    }

    fun deleteIdea(id: Long) {
        viewModelScope.launch {
            repository.deleteIdea(id)
        }
    }

    // Knowledge & Notes
    fun addKnowledge(title: String, content: String, url: String?, category: String, tags: String, notes: String) {
        viewModelScope.launch {
            repository.addKnowledge(
                KnowledgeEntity(
                    title = title,
                    content = content,
                    url = url,
                    category = category,
                    tags = tags,
                    notes = notes
                )
            )
            _userMessage.value = "Saved to Knowledge Vault"
        }
    }

    fun deleteKnowledge(id: Long) {
        viewModelScope.launch {
            repository.deleteKnowledge(id)
        }
    }

    fun addNote(title: String, content: String, category: String, tags: String) {
        viewModelScope.launch {
            repository.addNote(
                NoteEntity(
                    title = title,
                    content = content,
                    category = category,
                    tags = tags
                )
            )
            _userMessage.value = "Note saved"
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
            _userMessage.value = "Note updated"
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // Goals
    fun addGoal(title: String, description: String, timeframe: GoalTimeframe, category: String, progress: Int) {
        viewModelScope.launch {
            repository.addGoal(
                GoalEntity(
                    title = title,
                    description = description,
                    timeframe = timeframe,
                    category = category,
                    progressPercent = progress
                )
            )
            _userMessage.value = "Goal created"
        }
    }

    fun updateGoalProgress(goal: GoalEntity, progress: Int) {
        viewModelScope.launch {
            repository.updateGoal(goal.copy(progressPercent = progress, isCompleted = progress >= 100))
        }
    }

    fun deleteGoal(id: Long) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }

    // Study Sessions
    fun logStudySession(subject: String, minutes: Int, notes: String = "") {
        viewModelScope.launch {
            repository.logStudySession(subject, minutes, notes, _selectedDate.value)
            _userMessage.value = "Logged $minutes min of $subject"
        }
    }

    fun deleteStudySession(id: Long) {
        viewModelScope.launch {
            repository.deleteStudySession(id)
        }
    }

    // Timer controls
    fun setTimerSubject(subject: String) {
        _timerSubject.value = subject
    }

    fun startTimer() {
        if (_isTimerRunning.value) return
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value) {
                delay(1000)
                _timerSeconds.value += 1
            }
        }
    }

    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }

    fun stopAndSaveTimer() {
        pauseTimer()
        val durationMins = (_timerSeconds.value / 60).coerceAtLeast(1)
        logStudySession(_timerSubject.value, durationMins, "Tracked via Study Timer")
        _timerSeconds.value = 0
    }

    fun resetTimer() {
        pauseTimer()
        _timerSeconds.value = 0
    }

    // Credentials
    fun addCredential(service: String, username: String, secret: String, url: String, category: String, notes: String) {
        viewModelScope.launch {
            repository.addCredential(
                CredentialEntity(
                    serviceName = service,
                    username = username,
                    maskedSecret = secret,
                    url = url,
                    category = category,
                    notes = notes
                )
            )
            _userMessage.value = "Credential saved securely"
        }
    }

    fun deleteCredential(id: Long) {
        viewModelScope.launch {
            repository.deleteCredential(id)
        }
    }

    // Duas
    fun toggleDuaFavorite(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleDuaFavorite(id, isFav)
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Data Export JSON
    suspend fun exportDataJson(): String {
        val root = JSONObject()
        val recurringArray = JSONArray()
        recurringTasks.value.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("category", it.category)
            obj.put("priority", it.priority.name)
            obj.put("recurrenceType", it.recurrenceType.name)
            obj.put("duration", it.estimatedDurationMinutes)
            recurringArray.put(obj)
        }
        root.put("recurringTasks", recurringArray)

        val projectsArray = JSONArray()
        projects.value.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("technologies", it.technologies)
            obj.put("status", it.status.name)
            projectsArray.put(obj)
        }
        root.put("projects", projectsArray)

        root.put("exportDate", DateUtils.today())
        root.put("version", "1.0")
        return root.toString(2)
    }
}
