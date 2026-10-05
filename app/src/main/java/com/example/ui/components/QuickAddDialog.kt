package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Priority
import com.example.data.model.RecurrenceType
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onAddOneTimeTask: (title: String, desc: String, category: String, priority: Priority, date: String, time: String?, duration: Int) -> Unit,
    onAddRecurringTask: (title: String, desc: String, category: String, priority: Priority, type: RecurrenceType, time: String?, duration: Int) -> Unit,
    onAddReminder: (title: String, desc: String, category: String, priority: Priority, dueDate: String?) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Daily Task, 1: Recurring Routine, 2: Reminder
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("AI") }
    var selectedPriority by remember { mutableStateOf(Priority.MEDIUM) }
    var selectedDuration by remember { mutableIntStateOf(45) }
    var preferredTime by remember { mutableStateOf("") }
    var recurrenceType by remember { mutableStateOf(RecurrenceType.DAILY) }

    val categories = listOf("AI", "Programming", "Backend", "University", "Learning", "Health", "Personal", "Religious")
    val durations = listOf(15, 30, 45, 60, 90, 120)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("quick_add_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (selectedTab) {
                        0 -> "Add Task"
                        1 -> "New Daily Routine"
                        else -> "Set Reminder"
                    },
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_quick_add")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Type selector tabs
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Task") },
                        modifier = Modifier.testTag("tab_daily_task")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Routine") },
                        modifier = Modifier.testTag("tab_recurring")
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Reminder") },
                        modifier = Modifier.testTag("tab_reminder")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = {
                        Text(
                            when (selectedTab) {
                                0 -> "Task title (e.g. Study Neural Networks)"
                                1 -> "Routine title (e.g. Morning Problem Solving)"
                                else -> "Reminder (e.g. Submit lab report tomorrow)"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Details / Notes (optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quick_add_desc_input"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // If Recurring: Recurrence Type
                if (selectedTab == 1) {
                    Text(
                        text = "Frequency (Generates new instance each day)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RecurrenceType.entries.forEach { type ->
                            FilterChip(
                                selected = recurrenceType == type,
                                onClick = { recurrenceType = type },
                                label = { Text(type.displayName) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Category Chips
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Priority
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Priority.entries.forEach { prio ->
                        FilterChip(
                            selected = selectedPriority == prio,
                            onClick = { selectedPriority = prio },
                            label = { Text(prio.displayName) }
                        )
                    }
                }

                if (selectedTab != 2) {
                    Spacer(modifier = Modifier.height(10.dp))

                    // Duration chips
                    Text(
                        text = "Estimated Duration",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        durations.forEach { dur ->
                            FilterChip(
                                selected = selectedDuration == dur,
                                onClick = { selectedDuration = dur },
                                label = { Text("${dur}m") }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preferred Time
                    OutlinedTextField(
                        value = preferredTime,
                        onValueChange = { preferredTime = it },
                        label = { Text("Preferred Time (optional, e.g. 14:00)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    when (selectedTab) {
                        0 -> {
                            onAddOneTimeTask(
                                title.trim(),
                                description.trim(),
                                selectedCategory,
                                selectedPriority,
                                selectedDate,
                                preferredTime.ifBlank { null },
                                selectedDuration
                            )
                        }
                        1 -> {
                            onAddRecurringTask(
                                title.trim(),
                                description.trim(),
                                selectedCategory,
                                selectedPriority,
                                recurrenceType,
                                preferredTime.ifBlank { null },
                                selectedDuration
                            )
                        }
                        2 -> {
                            onAddReminder(
                                title.trim(),
                                description.trim(),
                                selectedCategory,
                                selectedPriority,
                                selectedDate
                            )
                        }
                    }
                    onDismiss()
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.testTag("save_quick_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
