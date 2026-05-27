package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConsolePanel(
    logs: List<String>,
    onClear: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Scroll to the bottom as new logs stream in
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    Surface(
        tonalElevation = 8.dp,
        color = Color(0xFF101014),
        modifier = modifier
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Terminal,
                        contentDescription = "Console Logs",
                        tint = Color(0xFF69F0AE),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "JS CONSOLE LOGS (${logs.size})",
                        color = Color(0xFFE0E0E6),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Row {
                    IconButton(
                        onClick = onClear,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear logs",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close console",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFF202028), thickness = 1.dp)

            // Log output area
            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "No Logs",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Console is empty.\nUse console.log() in JS to print inputs.",
                            color = Color.DarkGray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF070709))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(logs) { log ->
                        val logColor = when {
                            log.contains("ERROR", ignoreCase = true) -> Color(0xFFFF5252)
                            log.contains("WARNING", ignoreCase = true) -> Color(0xFFFFD740)
                            log.contains("INFO", ignoreCase = true) -> Color(0xFF40C4FF)
                            else -> Color(0xFF69F0AE)
                        }

                        Text(
                            text = log,
                            color = logColor,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 15.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (log.contains("ERROR", ignoreCase = true)) {
                                        Color(0x11FF5252)
                                    } else Color.Transparent,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .padding(vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}
