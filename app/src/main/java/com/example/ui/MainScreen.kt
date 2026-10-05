package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FolderSpecial
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.GlobalSearchDialog
import com.example.ui.components.QuickAddDialog
import com.example.ui.screens.PlannerScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.screens.TodayScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VaultScreen
import com.example.ui.viewmodel.StudentOsViewModel

enum class NavScreen(val label: String) {
    TODAY("Today"),
    PLANNER("Planner"),
    PROJECTS("Projects"),
    VAULT("Vault"),
    TOOLS("Tools")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: StudentOsViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(NavScreen.TODAY) }
    var showQuickAdd by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val projectTasks by viewModel.projectTasks.collectAsStateWithLifecycle()
    val ideas by viewModel.ideas.collectAsStateWithLifecycle()
    val knowledge by viewModel.knowledgeItems.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val goals by viewModel.goals.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val credentials by viewModel.credentials.collectAsStateWithLifecycle()
    val duas by viewModel.duas.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val isTimerRunning by viewModel.isTimerRunning.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.timerSeconds.collectAsStateWithLifecycle()
    val timerSubject by viewModel.timerSubject.collectAsStateWithLifecycle()

    // Handle back button: return to Today screen if in subscreen
    BackHandler(enabled = currentScreen != NavScreen.TODAY) {
        currentScreen = NavScreen.TODAY
    }

    // Show feedback messages
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "StudentOS",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showSearchDialog = true },
                        modifier = Modifier.testTag("top_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search OS")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentScreen == NavScreen.TODAY,
                    onClick = { currentScreen = NavScreen.TODAY },
                    icon = {
                        Icon(
                            if (currentScreen == NavScreen.TODAY) Icons.Default.Today else Icons.Outlined.Today,
                            contentDescription = "Today",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Today") },
                    modifier = Modifier.testTag("nav_today")
                )

                NavigationBarItem(
                    selected = currentScreen == NavScreen.PLANNER,
                    onClick = { currentScreen = NavScreen.PLANNER },
                    icon = {
                        Icon(
                            if (currentScreen == NavScreen.PLANNER) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                            contentDescription = "Planner",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Planner") },
                    modifier = Modifier.testTag("nav_planner")
                )

                NavigationBarItem(
                    selected = currentScreen == NavScreen.PROJECTS,
                    onClick = { currentScreen = NavScreen.PROJECTS },
                    icon = {
                        Icon(
                            if (currentScreen == NavScreen.PROJECTS) Icons.Default.FolderSpecial else Icons.Outlined.FolderSpecial,
                            contentDescription = "Projects",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Projects") },
                    modifier = Modifier.testTag("nav_projects")
                )

                NavigationBarItem(
                    selected = currentScreen == NavScreen.VAULT,
                    onClick = { currentScreen = NavScreen.VAULT },
                    icon = {
                        Icon(
                            if (currentScreen == NavScreen.VAULT) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Vault",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Vault") },
                    modifier = Modifier.testTag("nav_vault")
                )

                NavigationBarItem(
                    selected = currentScreen == NavScreen.TOOLS,
                    onClick = { currentScreen = NavScreen.TOOLS },
                    icon = {
                        Icon(
                            if (currentScreen == NavScreen.TOOLS) Icons.Default.Widgets else Icons.Outlined.Widgets,
                            contentDescription = "Tools",
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = { Text("Tools") },
                    modifier = Modifier.testTag("nav_tools")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentScreen) {
                NavScreen.TODAY -> {
                    TodayScreen(
                        selectedDate = selectedDate,
                        tasks = tasksForDate,
                        reminders = reminders,
                        activeProjects = projects,
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onCarryOverTask = { viewModel.carryOverTask(it) },
                        onDeleteTask = { viewModel.deleteTaskOccurrence(it) },
                        onPrevDay = { viewModel.goToPrevDay() },
                        onNextDay = { viewModel.goToNextDay() },
                        onGoToToday = { viewModel.goToToday() },
                        onOpenQuickAdd = { showQuickAdd = true },
                        onOpenTools = { currentScreen = NavScreen.TOOLS }
                    )
                }

                NavScreen.PLANNER -> {
                    PlannerScreen(
                        selectedDate = selectedDate,
                        tasks = tasksForDate,
                        reminders = reminders,
                        onSelectDate = { viewModel.setSelectedDate(it) },
                        onToggleTask = { viewModel.toggleTaskCompletion(it) },
                        onToggleReminder = { id, comp -> viewModel.toggleReminder(id, comp) },
                        onDeleteReminder = { viewModel.deleteReminder(it) },
                        onOpenQuickAdd = { showQuickAdd = true }
                    )
                }

                NavScreen.PROJECTS -> {
                    ProjectsScreen(
                        projects = projects,
                        allProjectTasks = projectTasks,
                        onToggleProjectTask = { viewModel.toggleProjectTask(it) },
                        onAddProjectTask = { id, title -> viewModel.addProjectTask(id, title) },
                        onDeleteProject = { viewModel.deleteProject(it) },
                        onCreateProject = { name, desc, status, prio, tech, dead, tList ->
                            viewModel.addProject(name, desc, status, prio, tech, dead, tList)
                        }
                    )
                }

                NavScreen.VAULT -> {
                    VaultScreen(
                        knowledgeItems = knowledge,
                        notes = notes,
                        ideas = ideas,
                        onAddKnowledge = { t, c, u, cat, tags, n -> viewModel.addKnowledge(t, c, u, cat, tags, n) },
                        onDeleteKnowledge = { viewModel.deleteKnowledge(it) },
                        onAddNote = { t, c, cat, tags -> viewModel.addNote(t, c, cat, tags) },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onAddIdea = { t, d, tags -> viewModel.addIdea(t, d, tags) },
                        onConvertIdeaToProject = { viewModel.convertIdeaToProject(it) },
                        onDeleteIdea = { viewModel.deleteIdea(it) }
                    )
                }

                NavScreen.TOOLS -> {
                    ToolsScreen(
                        isTimerRunning = isTimerRunning,
                        timerSeconds = timerSeconds,
                        timerSubject = timerSubject,
                        onTimerSubjectChange = { viewModel.setTimerSubject(it) },
                        onStartTimer = { viewModel.startTimer() },
                        onPauseTimer = { viewModel.pauseTimer() },
                        onSaveTimerSession = { viewModel.stopAndSaveTimer() },
                        onResetTimer = { viewModel.resetTimer() },
                        studySessions = studySessions,
                        onDeleteStudySession = { viewModel.deleteStudySession(it) },
                        goals = goals,
                        onAddGoal = { t, d, tf, cat, p -> viewModel.addGoal(t, d, tf, cat, p) },
                        onUpdateGoalProgress = { g, p -> viewModel.updateGoalProgress(g, p) },
                        onDeleteGoal = { viewModel.deleteGoal(it) },
                        duas = duas,
                        onToggleDuaFavorite = { id, fav -> viewModel.toggleDuaFavorite(id, fav) },
                        credentials = credentials,
                        onAddCredential = { s, u, p, url, cat, n -> viewModel.addCredential(s, u, p, url, cat, n) },
                        onDeleteCredential = { viewModel.deleteCredential(it) },
                        onExportJson = { viewModel.exportDataJson() },
                        completedTasksCount = tasksForDate.count { it.status == com.example.data.model.TaskStatus.COMPLETED },
                        totalTasksCount = tasksForDate.size,
                        activeProjectsCount = projects.count { it.status == com.example.data.model.ProjectStatus.IN_PROGRESS }
                    )
                }
            }
        }
    }

    if (showQuickAdd) {
        QuickAddDialog(
            selectedDate = selectedDate,
            onDismiss = { showQuickAdd = false },
            onAddOneTimeTask = { title, desc, cat, prio, date, time, dur ->
                viewModel.addOneTimeTask(title, desc, cat, prio, date, time, dur)
            },
            onAddRecurringTask = { title, desc, cat, prio, type, time, dur ->
                viewModel.addRecurringTask(title, desc, cat, prio, type, "1,2,3,4,5,6,7", selectedDate, null, time, dur)
            },
            onAddReminder = { title, desc, cat, prio, date ->
                viewModel.addReminder(title, desc, date, null, prio, cat)
            }
        )
    }

    if (showSearchDialog) {
        GlobalSearchDialog(
            query = searchQuery,
            onQueryChange = { viewModel.setSearchQuery(it) },
            results = searchResults,
            onDismiss = {
                showSearchDialog = false
                viewModel.setSearchQuery("")
            }
        )
    }
}
