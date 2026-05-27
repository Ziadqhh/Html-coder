package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.ImportExport
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ToggleOff
import androidx.compose.material.icons.filled.ToggleOn
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.WebPlugin

@Composable
fun PluginsManagerDialog(
    plugins: List<WebPlugin>,
    onTogglePlugin: (String) -> Unit,
    onImportPlugin: (String, String, String, String, Boolean) -> Unit,
    onDeletePlugin: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var isImportFormExpanded by remember { mutableStateOf(false) }

    // Dynamic state variables for Custom Import Form
    var customName by remember { mutableStateOf("") }
    var customDesc by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("Custom") }
    var customUrl by remember { mutableStateOf("") }
    var customIsScript by remember { mutableStateOf(true) } // True = JS, False = CSS

    val categories = listOf("All", "Styling Framework", "Animations", "UI Alerts", "Icons Suite", "Charts", "3D Graphics", "Typography", "Custom")

    // Filter plugins based on both Search input and Category Selection
    val filteredPlugins = remember(plugins, searchQuery, selectedCategory) {
        plugins.filter { plugin ->
            val matchesSearch = plugin.name.contains(searchQuery, ignoreCase = true) ||
                    plugin.description.contains(searchQuery, ignoreCase = true) ||
                    plugin.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "All" || plugin.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF101015)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF27C93F).copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = null,
                                tint = Color(0xFF27C93F),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "مستودع الملحقات والإضافات",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = "Developer Plugins Hub",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .background(Color(0xFF1F1F26), RoundedCornerShape(8.dp))
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Dialog",
                            tint = Color.LightGray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Importer Accordion Form
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isImportFormExpanded) Color(0xFF161622) else Color(0xFF16161C),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = if (isImportFormExpanded) Color(0xFF27C93F).copy(alpha = 0.4f) else Color(0xFF262630),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isImportFormExpanded = !isImportFormExpanded }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ImportExport,
                                    contentDescription = null,
                                    tint = Color(0xFF00FFCC),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "استيراد ملحق خارجي (Custom CDN)",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Inject external custom CSS/JS libraries",
                                        fontSize = 9.5.sp,
                                        color = Color.LightGray.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = isImportFormExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                                Spacer(modifier = Modifier.height(10.dp))

                                // Plugin Name input
                                Text(
                                    text = "اسم المكتبة / Plugin Name",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF88C0D0),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    placeholder = { Text("مثال: Animate UI / Lodash Utility", fontSize = 12.sp, color = Color.Gray) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF22222E),
                                        unfocusedContainerColor = Color(0xFF1E1E26),
                                        focusedBorderColor = Color(0xFF27C93F),
                                        unfocusedBorderColor = Color(0xFF2C2C38)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // CDN Address URL input
                                Text(
                                    text = "رابط المكتبة المباشر / Direct CDN URL (JS/CSS)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF88C0D0),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                OutlinedTextField(
                                    value = customUrl,
                                    onValueChange = { customUrl = it },
                                    placeholder = { Text("https://cdn.jsdelivr.net/npm/...", fontSize = 11.sp, color = Color.Gray, fontFamily = FontFamily.Monospace) },
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 11.sp, fontFamily = FontFamily.Monospace),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color(0xFF22222E),
                                        unfocusedContainerColor = Color(0xFF1E1E26),
                                        focusedBorderColor = Color(0xFF27C93F),
                                        unfocusedBorderColor = Color(0xFF2C2C38)
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Script VS Style selector capsules
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "نوع الملف / Asset Type",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray
                                        )
                                        Row(
                                            modifier = Modifier.padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            // JS Selection Card
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (customIsScript) Color(0xFFFFD740).copy(alpha = 0.15f) else Color(0xFF252530),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (customIsScript) Color(0xFFFFD740) else Color.Transparent,
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { customIsScript = true }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("JavaScript (.js)", color = if (customIsScript) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }

                                            // CSS Selection Card
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        color = if (!customIsScript) Color(0xFF40C4FF).copy(alpha = 0.15f) else Color(0xFF252530),
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .border(
                                                        width = 1.dp,
                                                        color = if (!customIsScript) Color(0xFF40C4FF) else Color.Transparent,
                                                        shape = RoundedCornerShape(6.dp)
                                                    )
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .clickable { customIsScript = false }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("CSS Stylesheet (.css)", color = if (!customIsScript) Color.White else Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            if (customName.isNotBlank() && customUrl.isNotBlank()) {
                                                onImportPlugin(
                                                    customName,
                                                    customDesc.ifBlank { "External integrated developer tool" },
                                                    customCategory,
                                                    customUrl,
                                                    customIsScript
                                                )
                                                // Reset Importer Form
                                                customName = ""
                                                customDesc = ""
                                                customUrl = ""
                                                isImportFormExpanded = false
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF27C93F),
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.padding(top = 14.dp)
                                    ) {
                                        Text("تثبيت / Install", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar Filter Area
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("البحث في الملحقات المتوفرة... / Search ...", fontSize = 12.sp, color = Color.Gray) },
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
                    textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 12.sp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF16161D),
                        unfocusedContainerColor = Color(0xFF121216),
                        focusedBorderColor = Color(0xFF27C93F),
                        unfocusedBorderColor = Color(0xFF262630)
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Category filter chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        val color = when (cat) {
                            "All" -> Color(0xFF88C1F3)
                            "Styling Framework" -> Color(0xFF40C4FF)
                            "Animations" -> Color(0xFFE040FB)
                            "UI Alerts" -> Color(0xFFFF5252)
                            "Icons Suite" -> Color(0xFF00FFCC)
                            "Charts" -> Color(0xFFFFD740)
                            "3D Graphics" -> Color(0xFFB388FF)
                            "Typography" -> Color(0xFFB48E00)
                            else -> Color(0xFF69F0AE)
                        }

                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSel) color.copy(alpha = 0.18f) else Color(0xFF1E1E26),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSel) color.copy(alpha = 0.8f) else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                color = if (isSel) Color.White else Color.Gray,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(8.dp))

                // Lazy Scrollable List of Filtered Available items
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredPlugins.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.Block, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(24.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "لم يتم العثور على أي ملحقات تطابق البحث",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "No matching plugins found in category",
                                        color = Color.Gray.copy(alpha = 0.6f),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }

                    items(filteredPlugins) { plugin ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = 1.dp,
                                    color = if (plugin.isEnabled) Color(0xFF27C93F).copy(alpha = 0.35f) else Color(0xFF242430),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = if (plugin.isEnabled) Color(0xFF141A16) else Color(0xFF16161D)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    // Row showing Title, Category Label
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = plugin.name,
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (plugin.isEnabled) Color.White else Color(0xFFE4E4EB)
                                        )

                                        Box(
                                            modifier = Modifier
                                                .background(
                                                    color = when (plugin.category) {
                                                        "Styling Framework" -> Color(0xFF40C4FF).copy(alpha = 0.1f)
                                                        "Animations" -> Color(0xFFE040FB).copy(alpha = 0.1f)
                                                        "UI Alerts" -> Color(0xFFFF5252).copy(alpha = 0.1f)
                                                        "Icons Suite" -> Color(0xFF00FFCC).copy(alpha = 0.1f)
                                                        "Typography" -> Color(0xFFFFD740).copy(alpha = 0.1f)
                                                        else -> Color(0xFF69F0AE).copy(alpha = 0.1f)
                                                    },
                                                    shape = RoundedCornerShape(4.dp)
                                                )
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = plugin.category,
                                                fontSize = 8.5.sp,
                                                color = when (plugin.category) {
                                                    "Styling Framework" -> Color(0xFF40C4FF)
                                                    "Animations" -> Color(0xFFE040FB)
                                                    "UI Alerts" -> Color(0xFFFF5252)
                                                    "Icons Suite" -> Color(0xFF00FFCC)
                                                    "Typography" -> Color(0xFFFFD740)
                                                    else -> Color(0xFF69F0AE)
                                                },
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }

                                        if (plugin.isCustom) {
                                            Box(
                                                modifier = Modifier
                                                    .background(Color(0xFF00FFCC).copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("Custom", color = Color(0xFF00FFCC), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = plugin.description,
                                        fontSize = 11.sp,
                                        color = Color.LightGray.copy(alpha = 0.8f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 15.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (plugin.isScript) Icons.Default.Javascript else Icons.Default.Code,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (plugin.isScript) "JavaScript SDK" else "CSS Stylesheet",
                                                fontSize = 9.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        // Cut-off CDN Address view
                                        val displayUrl = if (plugin.url.startsWith("http")) {
                                            plugin.url.substringAfter("https://").take(32) + "..."
                                        } else {
                                            "Custom Script Resource Code"
                                        }
                                        Text(
                                            text = "CDN: $displayUrl",
                                            fontSize = 9.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.Gray.copy(alpha = 0.7f),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                // Switch controller or actions group
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Switch(
                                        checked = plugin.isEnabled,
                                        onCheckedChange = { onTogglePlugin(plugin.id) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = Color(0xFF27C93F),
                                            uncheckedThumbColor = Color.Gray,
                                            uncheckedTrackColor = Color(0xFF242430)
                                        )
                                    )

                                    if (plugin.isCustom) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        IconButton(
                                            onClick = { onDeletePlugin(plugin.id) },
                                            modifier = Modifier
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                                .size(30.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete Custom Plugin",
                                                tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
