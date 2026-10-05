package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.IdeaEntity
import com.example.data.model.KnowledgeEntity
import com.example.data.model.NoteEntity
import com.example.ui.components.IdeaCard
import com.example.ui.components.KnowledgeCard
import com.example.ui.components.NoteCard

import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    knowledgeItems: List<KnowledgeEntity>,
    notes: List<NoteEntity>,
    ideas: List<IdeaEntity>,
    onAddKnowledge: (title: String, content: String, url: String?, category: String, tags: String, notes: String) -> Unit,
    onDeleteKnowledge: (Long) -> Unit,
    onAddNote: (title: String, content: String, category: String, tags: String) -> Unit,
    onDeleteNote: (Long) -> Unit,
    onAddIdea: (title: String, desc: String, tags: String) -> Unit,
    onConvertIdeaToProject: (IdeaEntity) -> Unit,
    onDeleteIdea: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Knowledge, 1: Notes, 2: Ideas
    var filterQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("vault_screen_list"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Tabs
            item {
                SecondaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Vault (${knowledgeItems.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Notes (${notes.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Ideas (${ideas.size})") }
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = filterQuery,
                    onValueChange = { filterQuery = it },
                    placeholder = {
                        Text(
                            when (selectedTab) {
                                0 -> "Search articles, algorithms, docs..."
                                1 -> "Search notes & lectures..."
                                else -> "Search ideas..."
                            }
                        )
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (filterQuery.isNotEmpty()) {
                            IconButton(onClick = { filterQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            when (selectedTab) {
                0 -> {
                    val filtered = knowledgeItems.filter {
                        filterQuery.isBlank() || it.title.contains(filterQuery, ignoreCase = true) ||
                                it.content.contains(filterQuery, ignoreCase = true) ||
                                it.tags.contains(filterQuery, ignoreCase = true) ||
                                it.category.contains(filterQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyState(message = "No knowledge items found. Tap '+' to save an article, doc, or tutorial.")
                        }
                    } else {
                        items(filtered, key = { it.id }) { item ->
                            KnowledgeCard(item = item, onDelete = { onDeleteKnowledge(item.id) })
                        }
                    }
                }
                1 -> {
                    val filtered = notes.filter {
                        filterQuery.isBlank() || it.title.contains(filterQuery, ignoreCase = true) ||
                                it.content.contains(filterQuery, ignoreCase = true) ||
                                it.tags.contains(filterQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyState(message = "No notes recorded. Tap '+' to write lecture or technical notes.")
                        }
                    } else {
                        items(filtered, key = { it.id }) { note ->
                            NoteCard(note = note, onDelete = { onDeleteNote(note.id) })
                        }
                    }
                }
                2 -> {
                    val filtered = ideas.filter {
                        filterQuery.isBlank() || it.title.contains(filterQuery, ignoreCase = true) ||
                                it.description.contains(filterQuery, ignoreCase = true) ||
                                it.tags.contains(filterQuery, ignoreCase = true)
                    }

                    if (filtered.isEmpty()) {
                        item {
                            EmptyState(message = "No ideas saved yet. Tap '+' to capture project or startup ideas.")
                        }
                    } else {
                        items(filtered, key = { it.id }) { idea ->
                            IdeaCard(
                                idea = idea,
                                onConvertToProject = { onConvertIdeaToProject(idea) },
                                onDelete = { onDeleteIdea(idea.id) }
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("vault_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Item")
        }

        if (showAddDialog) {
            when (selectedTab) {
                0 -> AddKnowledgeDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = onAddKnowledge
                )
                1 -> AddNoteDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = onAddNote
                )
                2 -> AddIdeaDialog(
                    onDismiss = { showAddDialog = false },
                    onSave = onAddIdea
                )
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(20.dp)
        )
    }
}

@Composable
fun AddKnowledgeDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, url: String?, category: String, tags: String, notes: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("AI") }
    var tags by remember { mutableStateOf("#AI #DeepLearning") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save to Knowledge Vault", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Concept") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Explanation / Summary") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Reference URL (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. AI, Backend, CS)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (e.g. #Transformers #PyTorch)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && content.isNotBlank()) {
                        onSave(title.trim(), content.trim(), url.ifBlank { null }, category.trim(), tags.trim(), "")
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank() && content.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddNoteDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, content: String, category: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("University") }
    var tags by remember { mutableStateOf("#Notes") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Note", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Note Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Note Content") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), content.trim(), category.trim(), tags.trim())
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Note")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddIdeaDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, tags: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("#Idea #Software") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Capture Quick Idea", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Idea Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), tags.trim())
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Save Idea")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
