package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.Project
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProjectListDialog(
    projects: List<Project>,
    activeProject: Project?,
    onLoadProject: (Project) -> Unit,
    onCreateProject: (String) -> Unit,
    onDeleteProject: (Project) -> Unit,
    onDismiss: () -> Unit
) {
    var newProjectName by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var projectToDelete by remember { mutableStateOf<Project?>(null) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }

    // Filter projects dynamically based on search query
    val filteredProjects = remember(projects, searchQuery) {
        if (searchQuery.isBlank()) {
            projects
        } else {
            projects.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF13131A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with Workspace Title and Close icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Workspace",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Project Hub",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 1: Create Project Row View
                Text(
                    text = "CREATE NEW WORKSPACE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newProjectName,
                        onValueChange = { newProjectName = it },
                        placeholder = { Text("Enter workspace name...", fontSize = 13.sp, color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 14.sp),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF1E1E24),
                            unfocusedContainerColor = Color(0xFF16161D),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFF2E2E38)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newProjectName.isNotBlank()) {
                                onCreateProject(newProjectName)
                                newProjectName = ""
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Section 2: Search Query Input Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search projects...", fontSize = 13.sp, color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 13.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF1E1E24),
                        unfocusedContainerColor = Color(0xFF16161D),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color(0xFF2E2E38)
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.06f))
                Spacer(modifier = Modifier.height(10.dp))

                // Section 3: Project lists scroll
                val totalProjsLabel = if (filteredProjects.size == projects.size) "${projects.size}" else "${filteredProjects.size} of ${projects.size}"
                Text(
                    text = "SAVED WORKSPACES ($totalProjsLabel)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (filteredProjects.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No workspaces found",
                                    color = Color.Gray,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    items(filteredProjects) { project ->
                        val isActive = activeProject?.id == project.id
                        val fileSize = (project.html.length + project.css.length + project.js.length)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .combinedClickable(
                                    onClick = {
                                        onLoadProject(project)
                                        onDismiss()
                                    },
                                    onLongClick = {
                                        projectToDelete = project
                                    }
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isActive) {
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                } else {
                                    Color(0xFF1A1A24)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isActive) MaterialTheme.colorScheme.primary else Color(0xFF2E2E3A)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = project.name,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = if (isActive) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                Color.White
                                            },
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (isActive) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Active",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Custom visual indicators for code composition chips
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        CodeLanguageBadge(label = "HTML5", color = Color(0xFFFF5252))
                                        CodeLanguageBadge(label = "CSS3", color = Color(0xFF40C4FF))
                                        CodeLanguageBadge(label = "JS", color = Color(0xFFFFD740))

                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${dateFormat.format(Date(project.lastModifiedAt))} • ${fileSize}B",
                                            fontSize = 10.sp,
                                            color = Color.Gray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        projectToDelete = project
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete workspace",
                                        tint = if (isActive) {
                                            Color(0xFFFF5252).copy(alpha = 0.6f)
                                        } else {
                                            Color.Gray
                                        },
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    // Confirmation delete prompt
    if (projectToDelete != null) {
        val target = projectToDelete!!
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text(text = "Delete workspace?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text(text = "Are you sure you want to delete '${target.name}'? This cannot be undone.", color = Color.LightGray) },
            containerColor = Color(0xFF202028),
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProject(target)
                        projectToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF5252),
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) {
                    Text("Cancel", color = Color.LightGray)
                }
            }
        )
    }
}

@Composable
fun CodeLanguageBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 8.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
