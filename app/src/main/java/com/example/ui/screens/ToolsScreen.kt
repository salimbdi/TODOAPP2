package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CredentialEntity
import com.example.data.model.DuaEntity
import com.example.data.model.GoalEntity
import com.example.data.model.GoalTimeframe
import com.example.data.model.StudySessionEntity
import com.example.ui.components.CredentialCard
import com.example.ui.components.DuaCard
import com.example.ui.components.GoalCard
import com.example.ui.components.StudyTimerCard
import com.example.ui.theme.Emerald80
import com.example.ui.theme.TaskCompletedGreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ToolsScreen(
    isTimerRunning: Boolean,
    timerSeconds: Int,
    timerSubject: String,
    onTimerSubjectChange: (String) -> Unit,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onSaveTimerSession: () -> Unit,
    onResetTimer: () -> Unit,
    studySessions: List<StudySessionEntity>,
    onDeleteStudySession: (Long) -> Unit,
    goals: List<GoalEntity>,
    onAddGoal: (title: String, desc: String, timeframe: GoalTimeframe, category: String, progress: Int) -> Unit,
    onUpdateGoalProgress: (GoalEntity, Int) -> Unit,
    onDeleteGoal: (Long) -> Unit,
    duas: List<DuaEntity>,
    onToggleDuaFavorite: (Long, Boolean) -> Unit,
    credentials: List<CredentialEntity>,
    onAddCredential: (service: String, user: String, pass: String, url: String, category: String, notes: String) -> Unit,
    onDeleteCredential: (Long) -> Unit,
    onExportJson: suspend () -> String,
    completedTasksCount: Int,
    totalTasksCount: Int,
    activeProjectsCount: Int,
    modifier: Modifier = Modifier
) {
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Study & Stats, 1: Goals, 2: Duas & Adhkar, 3: Credentials, 4: Review & Backup
    val context = LocalContext.current
    var exportedJson by remember { mutableStateOf<String?>(null) }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    var showAddCredentialDialog by remember { mutableStateOf(false) }

    val totalStudyMinutes = studySessions.sumOf { it.durationMinutes }
    val studyHours = totalStudyMinutes / 60
    val studyRemainingMins = totalStudyMinutes % 60

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("tools_screen_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // Section Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val sections = listOf(
                    "Study & Timer" to Icons.Default.Timer,
                    "Goals" to Icons.Default.BarChart,
                    "Duas & Adhkar" to Icons.Default.MenuBook,
                    "Credentials" to Icons.Default.Lock,
                    "Review & Backup" to Icons.Default.FileDownload
                )

                sections.forEachIndexed { index, (name, icon) ->
                    FilterChip(
                        selected = selectedSection == index,
                        onClick = { selectedSection = index },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(name)
                            }
                        }
                    )
                }
            }
        }

        when (selectedSection) {
            0 -> {
                // Study Focus Timer
                item {
                    StudyTimerCard(
                        isRunning = isTimerRunning,
                        seconds = timerSeconds,
                        subject = timerSubject,
                        onSubjectChange = onTimerSubjectChange,
                        onStart = onStartTimer,
                        onPause = onPauseTimer,
                        onSaveSession = onSaveTimerSession,
                        onReset = onResetTimer
                    )
                }

                // Study Statistics Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "STUDY EFFORT DASHBOARD",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${studyHours}h ${studyRemainingMins}m",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text("Total Logged", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${studySessions.size}",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text("Sessions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = if (studySessions.isNotEmpty()) "${totalStudyMinutes / studySessions.size}m" else "0m",
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                        color = TaskCompletedGreen
                                    )
                                    Text("Avg Session", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                // Recent sessions
                item {
                    Text(
                        text = "RECENT STUDY SESSIONS",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(studySessions, key = { it.id }) { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = session.subject,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${session.durationMinutes} min • ${session.date} ${if (session.notes.isNotBlank()) "• ${session.notes}" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { onDeleteStudySession(session.id) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            1 -> {
                // Goals Section
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GOALS & MILESTONES",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = { showAddGoalDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New Goal")
                        }
                    }
                }

                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onUpdateProgress = { onUpdateGoalProgress(goal, it) },
                        onDelete = { onDeleteGoal(goal.id) }
                    )
                }
            }

            2 -> {
                // Authentic Duas & Adhkar
                item {
                    Text(
                        text = "AUTHENTIC DUAS & ADHKAR",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(duas, key = { it.id }) { dua ->
                    DuaCard(
                        dua = dua,
                        onToggleFavorite = { onToggleDuaFavorite(dua.id, dua.isFavorite) }
                    )
                }
            }

            3 -> {
                // Credentials Vault
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SENSITIVE CREDENTIALS VAULT",
                            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Button(onClick = { showAddCredentialDialog = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Key")
                        }
                    }
                }

                items(credentials, key = { it.id }) { cred ->
                    CredentialCard(
                        cred = cred,
                        onDelete = { onDeleteCredential(cred.id) }
                    )
                }
            }

            4 -> {
                // Weekly Productivity Review & JSON Backup
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "WEEKLY PRODUCTIVITY REVIEW",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            val rate = if (totalTasksCount > 0) (completedTasksCount * 100) / totalTasksCount else 0

                            Text(
                                text = "Task Completion Rate: $rate%",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$completedTasksCount completed out of $totalTasksCount total scheduled tasks today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Active Engineering Projects: $activeProjectsCount",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Total Logged Study Time: ${studyHours}h ${studyRemainingMins}m",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "DATA BACKUP & EXPORT",
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Export all your recurring tasks, projects, notes, and goals to JSON for backup or sharing.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    CoroutineScope(Dispatchers.Main).launch {
                                        val json = onExportJson()
                                        exportedJson = json
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.FileDownload, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export JSON")
                            }

                            if (exportedJson != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = exportedJson!!,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            maxLines = 8
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(ClipData.newPlainText("backup", exportedJson))
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Icon(Icons.Default.ContentCopy, contentDescription = null)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Copy JSON to Clipboard")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showAddGoalDialog) {
        AddGoalDialog(
            onDismiss = { showAddGoalDialog = false },
            onSave = onAddGoal
        )
    }

    if (showAddCredentialDialog) {
        AddCredentialDialog(
            onDismiss = { showAddCredentialDialog = false },
            onSave = onAddCredential
        )
    }
}

@Composable
fun AddGoalDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, timeframe: GoalTimeframe, category: String, progress: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var timeframe by remember { mutableStateOf(GoalTimeframe.WEEKLY) }
    var category by remember { mutableStateOf("AI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set New Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Description & Milestone") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                Text("Timeframe", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    GoalTimeframe.entries.forEach { tf ->
                        FilterChip(
                            selected = timeframe == tf,
                            onClick = { timeframe = tf },
                            label = { Text(tf.displayName) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), desc.trim(), timeframe, category, 0)
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Goal")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddCredentialDialog(
    onDismiss: () -> Unit,
    onSave: (service: String, user: String, pass: String, url: String, category: String, notes: String) -> Unit
) {
    var service by remember { mutableStateOf("") }
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Development") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Encrypted Credential", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = service,
                    onValueChange = { service = it },
                    label = { Text("Service (e.g. GitHub, AWS, HuggingFace)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("Username / Email") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    label = { Text("Password or API Token") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (service.isNotBlank() && pass.isNotBlank()) {
                        onSave(service.trim(), user.trim(), pass.trim(), url.trim(), category, "")
                        onDismiss()
                    }
                },
                enabled = service.isNotBlank() && pass.isNotBlank()
            ) {
                Text("Save Credential")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
