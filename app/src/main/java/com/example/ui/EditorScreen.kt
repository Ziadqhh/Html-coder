package com.example.ui

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material.icons.filled.Square
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CodeEditor
import com.example.ui.components.ConsolePanel
import com.example.ui.components.HtmlPreview
import com.example.ui.components.ProjectListDialog

enum class LayoutMode {
    EDITOR_ONLY,
    PREVIEW_ONLY,
    SPLIT_SCREEN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    viewModel: EditorViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // State bindings
    val projects by viewModel.allProjects.collectAsState()
    val currentProject by viewModel.currentProject.collectAsState()
    val activeTab by viewModel.activeTab.collectAsState()
    val htmlCode by viewModel.htmlBuffer.collectAsState()
    val cssCode by viewModel.cssBuffer.collectAsState()
    val jsCode by viewModel.jsBuffer.collectAsState()
    val isLivePreview by viewModel.isLivePreviewEnabled.collectAsState()
    val previewTrigger by viewModel.previewTrigger.collectAsState()
    val editorTheme by viewModel.editorTheme.collectAsState()
    val consoleLogs by viewModel.consoleLogs.collectAsState()
    val projectPlugins by viewModel.projectPlugins.collectAsState()

    // UI state configurations
    var isWorkspaceDialogOpen by remember { mutableStateOf(false) }
    var isPluginsDialogOpen by remember { mutableStateOf(false) }
    var isThemeMenuExpanded by remember { mutableStateOf(false) }
    var isConsoleExpanded by remember { mutableStateOf(false) }
    var portraitLayoutMode by remember { mutableStateOf(LayoutMode.SPLIT_SCREEN) }

    // Resolve unified visual code buffer values based on active tab
    val activeCodeValue = when (activeTab) {
        EditorTab.HTML -> htmlCode
        EditorTab.CSS -> cssCode
        EditorTab.JS -> jsCode
    }

    val onCodeUpdate: (String) -> Unit = { updated ->
        when (activeTab) {
            EditorTab.HTML -> viewModel.updateHtml(updated)
            EditorTab.CSS -> viewModel.updateCss(updated)
            EditorTab.JS -> viewModel.updateJs(updated)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = currentProject?.name ?: "No workspace loaded",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isLivePreview) "● Web Live Mode Active" else "○ Manual Compilation Mode",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLivePreview) Color(0xFF69F0AE) else Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { isWorkspaceDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = "Workspaces",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    // Plugins hub action button with live badge indicators
                    IconButton(
                        onClick = { isPluginsDialogOpen = true },
                        enabled = currentProject != null
                    ) {
                        BadgedBox(
                            badge = {
                                val activePlugsCount = projectPlugins.count { it.isEnabled }
                                if (activePlugsCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFF27C93F)
                                    ) {
                                        Text("$activePlugsCount", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = "Plugins Hub",
                                tint = if (projectPlugins.any { it.isEnabled }) Color(0xFF27C93F) else Color.LightGray
                            )
                        }
                    }

                    // Duplicate project action
                    IconButton(
                        onClick = {
                            viewModel.duplicateCurrentProject()
                            Toast.makeText(context, "Project Cloned", Toast.LENGTH_SHORT).show()
                        },
                        enabled = currentProject != null
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Clone project",
                            tint = Color.LightGray
                        )
                    }

                    // Live reload mode toggle switch button
                    IconButton(
                        onClick = {
                            viewModel.setLivePreviewEnabled(!isLivePreview)
                            val msg = if (!isLivePreview) "Live hot-reload active" else "Manual refresh required"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            imageVector = if (isLivePreview) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = "Toggle Reload",
                            tint = if (isLivePreview) Color(0xFF69F0AE) else Color.Gray
                        )
                    }

                    // Editor theme custom configurations expanded panel
                    Box {
                        IconButton(onClick = { isThemeMenuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Editor Themes",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        DropdownMenu(
                            expanded = isThemeMenuExpanded,
                            onDismissRequest = { isThemeMenuExpanded = false },
                            modifier = Modifier.background(Color(0xFF202028))
                        ) {
                            EditorTheme.values().forEach { style ->
                                val label = when (style) {
                                    EditorTheme.DARK_MONOCHROME -> "Slate Monochrome"
                                    EditorTheme.OBSIDIAN_DRACULA -> "Obsidian Dracula"
                                    EditorTheme.COBALT_DEEP -> "Cobalt Cyan"
                                    EditorTheme.CYBERPUNK -> "Neon Cyberpunk"
                                    EditorTheme.LIGHT_SAND -> "Light Warm Box"
                                }
                                DropdownMenuItem(
                                    text = { 
                                        Text(
                                            text = label, 
                                            color = if (editorTheme == style) Color.White else Color.Gray, 
                                            fontSize = 13.sp,
                                            fontWeight = if (editorTheme == style) FontWeight.Bold else FontWeight.Medium
                                        ) 
                                    },
                                    onClick = {
                                        viewModel.setEditorTheme(style)
                                        isThemeMenuExpanded = false
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Code,
                                            contentDescription = null,
                                            tint = if (editorTheme == style) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Mobile custom display viewport modes toggle button
                    if (!isLandscape) {
                        IconButton(
                            onClick = {
                                portraitLayoutMode = when (portraitLayoutMode) {
                                    LayoutMode.EDITOR_ONLY -> LayoutMode.SPLIT_SCREEN
                                    LayoutMode.SPLIT_SCREEN -> LayoutMode.PREVIEW_ONLY
                                    LayoutMode.PREVIEW_ONLY -> LayoutMode.EDITOR_ONLY
                                }
                            }
                        ) {
                            val layoutIcon = when (portraitLayoutMode) {
                                LayoutMode.EDITOR_ONLY -> Icons.Default.Square
                                LayoutMode.PREVIEW_ONLY -> Icons.Default.ViewColumn
                                LayoutMode.SPLIT_SCREEN -> Icons.Default.Splitscreen
                            }
                            Icon(
                                imageVector = layoutIcon,
                                contentDescription = "Toggle screen split mode",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F0F14),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            // Console expand toggle button at bottom
            Surface(
                color = Color(0xFF0A0A0C),
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isConsoleExpanded = true }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        BadgedBox(
                            badge = {
                                if (consoleLogs.isNotEmpty()) {
                                    Badge(
                                        containerColor = if (consoleLogs.any { it.contains("ERROR", true) }) Color(0xFFFF5252) else Color(0xFF27C93F)
                                    ) {
                                        Text("${consoleLogs.size}", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = "Console",
                                tint = if (consoleLogs.isNotEmpty()) {
                                    if (consoleLogs.any { it.contains("ERROR", true) }) Color(0xFFFF5252) else Color(0xFF27C93F)
                                } else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "DEVELOPER CONSOLE",
                            color = Color(0xFFB0B0C4),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (consoleLogs.isNotEmpty()) {
                            val lastLog = consoleLogs.last()
                            val color = if (lastLog.contains("ERROR", true)) Color(0xFFFF5252) else Color(0xFF27C93F)
                            Text(
                                text = lastLog.take(24) + if (lastLog.length > 24) "..." else "",
                                color = color.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(end = 12.dp),
                                maxLines = 1
                            )
                        }
                        Text(
                            text = "EXPAND",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFF0C0C0F))
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isLandscape) {
                    // Landscape Mode - Horizontal split editor panel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left Pane: Codes workspace
                        Column(
                            modifier = Modifier
                                .weight(1.1f)
                                .fillMaxHeight()
                        ) {
                            EditorTabsRow(activeTab = activeTab, onTabSelect = viewModel::selectTab)
                            CodeEditor(
                                code = activeCodeValue,
                                onCodeChange = onCodeUpdate,
                                activeTab = activeTab,
                                theme = editorTheme,
                                onInsertSnippet = viewModel::insertTextAtSelection,
                                onFormat = viewModel::formatSelectedBuffer,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        VerticalDivider(
                            color = Color.White.copy(alpha = 0.08f),
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                        )

                        // Right Pane: Local browser preview window
                        Column(
                            modifier = Modifier
                                .weight(1.0f)
                                .fillMaxHeight()
                        ) {
                            BrowserWindowChrome(
                                url = "http://localhost:8080/index.html",
                                onRefresh = viewModel::triggerManualPreviewRun
                            )
                            HtmlPreview(
                                combinedHtml = viewModel.getCombinedHtmlOutput(),
                                isLivePreview = isLivePreview,
                                triggerToken = previewTrigger,
                                onLogReceived = viewModel::addConsoleLog,
                                onConsoleClear = viewModel::clearConsoleLogs,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                } else {
                    // Portrait Mode - Single or Split orientation
                    when (portraitLayoutMode) {
                        LayoutMode.EDITOR_ONLY -> {
                            EditorTabsRow(activeTab = activeTab, onTabSelect = viewModel::selectTab)
                            CodeEditor(
                                code = activeCodeValue,
                                onCodeChange = onCodeUpdate,
                                activeTab = activeTab,
                                theme = editorTheme,
                                onInsertSnippet = viewModel::insertTextAtSelection,
                                onFormat = viewModel::formatSelectedBuffer,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        LayoutMode.PREVIEW_ONLY -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                BrowserWindowChrome(
                                    url = "http://localhost:8080/index.html",
                                    onRefresh = viewModel::triggerManualPreviewRun
                                )
                                HtmlPreview(
                                    combinedHtml = viewModel.getCombinedHtmlOutput(),
                                    isLivePreview = isLivePreview,
                                    triggerToken = previewTrigger,
                                    onLogReceived = viewModel::addConsoleLog,
                                    onConsoleClear = viewModel::clearConsoleLogs,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                        LayoutMode.SPLIT_SCREEN -> {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Top Half Editor screen
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1.1f)
                                ) {
                                    EditorTabsRow(activeTab = activeTab, onTabSelect = viewModel::selectTab)
                                    CodeEditor(
                                        code = activeCodeValue,
                                        onCodeChange = onCodeUpdate,
                                        activeTab = activeTab,
                                        theme = editorTheme,
                                        onInsertSnippet = viewModel::insertTextAtSelection,
                                        onFormat = viewModel::formatSelectedBuffer,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.08f),
                                    thickness = 1.dp,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                // Bottom Half live sandbox
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(0.9f)
                                ) {
                                    BrowserWindowChrome(
                                        url = "http://localhost:8080/index.html",
                                        onRefresh = viewModel::triggerManualPreviewRun
                                    )
                                    HtmlPreview(
                                        combinedHtml = viewModel.getCombinedHtmlOutput(),
                                        isLivePreview = isLivePreview,
                                        triggerToken = previewTrigger,
                                        onLogReceived = viewModel::addConsoleLog,
                                        onConsoleClear = viewModel::clearConsoleLogs,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Expanded floating Console logger sliding sheet from bottom options
            AnimatedVisibility(
                visible = isConsoleExpanded,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { isConsoleExpanded = false },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    ConsolePanel(
                        logs = consoleLogs,
                        onClear = viewModel::clearConsoleLogs,
                        onClose = { isConsoleExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                            .clickable(enabled = false) { /* Stop clicks traversing */ }
                    )
                }
            }
        }
    }

    // Modal Workspace dialog launcher logic
    if (isWorkspaceDialogOpen) {
        ProjectListDialog(
            projects = projects,
            activeProject = currentProject,
            onLoadProject = viewModel::loadProject,
            onCreateProject = viewModel::createNewProject,
            onDeleteProject = viewModel::deleteProject,
            onDismiss = { isWorkspaceDialogOpen = false }
        )
    }

    // Modal Plugins manager dialog launcher logic
    if (isPluginsDialogOpen) {
        com.example.ui.components.PluginsManagerDialog(
            plugins = projectPlugins,
            onTogglePlugin = viewModel::togglePlugin,
            onImportPlugin = viewModel::addCustomPlugin,
            onDeletePlugin = viewModel::deleteCustomPlugin,
            onDismiss = { isPluginsDialogOpen = false }
        )
    }
}

// Sub-component switcher tabs styled with capsule cards
@Composable
private fun EditorTabsRow(
    activeTab: EditorTab,
    onTabSelect: (EditorTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF131318),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EditorTab.values().forEach { tab ->
                val isSelected = activeTab == tab
                val color = when (tab) {
                    EditorTab.HTML -> Color(0xFFFF5252) // Neon Red HTML
                    EditorTab.CSS -> Color(0xFF40C4FF)  // Cyans CSS
                    EditorTab.JS -> Color(0xFFFFD740)   // Amber Javascript
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .background(
                            color = if (isSelected) color.copy(alpha = 0.15f) else Color(0xFF1E1E24),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onTabSelect(tab) }
                        .then(
                            if (isSelected) Modifier.drawBehind {
                                val strokeWidth = 3.dp.toPx()
                                drawLine(
                                    color = color,
                                    start = androidx.compose.ui.geometry.Offset(0f, size.height - strokeWidth / 2),
                                    end = androidx.compose.ui.geometry.Offset(size.width, size.height - strokeWidth / 2),
                                    strokeWidth = strokeWidth
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isSelected) color else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tab.name,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 11.5.sp,
                            color = if (isSelected) Color.White else Color.Gray,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Built-in device browser visual outline helper
@Composable
fun BrowserWindowChrome(
    url: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF16161E))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // MacOS style windows control lights 🔴 🟡 🟢
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFF5F56), RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.size(8.dp).background(Color(0xFFFFBD2E), RoundedCornerShape(4.dp)))
            Box(modifier = Modifier.size(8.dp).background(Color(0xFF27C93F), RoundedCornerShape(4.dp)))
        }

        // Search Bar styled Address input bar
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp)
                .background(Color(0xFF0A0A0E), RoundedCornerShape(6.dp))
                .height(26.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reload Preview",
                    tint = Color.Gray,
                    modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = url,
                    color = Color.LightGray,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active indicators
        Box(
            modifier = Modifier
                .background(Color(0xFF27C93F).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Text(
                text = "PORT:8080",
                color = Color(0xFF27C93F),
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
