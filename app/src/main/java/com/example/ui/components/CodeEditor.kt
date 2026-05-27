package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.WrapText
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.EditorTab
import com.example.ui.EditorTheme

@Composable
fun CodeEditor(
    code: String,
    onCodeChange: (String) -> Unit,
    activeTab: EditorTab,
    theme: EditorTheme,
    onInsertSnippet: (String) -> Unit,
    onFormat: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Persistent user preferences inside editor view
    var fontSize by rememberSaveable { mutableStateOf(13.5f) }
    var wordWrap by rememberSaveable { mutableStateOf(false) }
    var showSettingsTray by remember { mutableStateOf(false) }

    // Convert string to TextFieldValue to support selection-aware insertions and cursor positions
    var textFieldValueState by remember(code) {
        mutableStateOf(
            TextFieldValue(
                text = code,
                selection = TextRange(code.length)
            )
        )
    }

    // Visual colors based on selected EditorTheme
    val (editorBgColor, editorTextColor, editorGutterBg, editorGutterTextColor, cursorBrushColor) = when (theme) {
        EditorTheme.DARK_MONOCHROME -> listOf(
            Color(0xFF0F0F12), Color(0xFFE4E4EB), Color(0xFF16161D), Color(0xFF6F6F76), Color(0xFFE4E4EB)
        )
        EditorTheme.OBSIDIAN_DRACULA -> listOf(
            Color(0xFF1E1E2E), Color(0xFFF8F8F2), Color(0xFF181825), Color(0xFF6272A4), Color(0xFFBD93F9)
        )
        EditorTheme.COBALT_DEEP -> listOf(
            Color(0xFF001B33), Color(0xFFE0F7FA), Color(0xFF001224), Color(0xFF78909C), Color(0xFFFFC107)
        )
        EditorTheme.CYBERPUNK -> listOf(
            Color(0xFF0B0415), Color(0xFFE0E0FF), Color(0xFF130722), Color(0xFF7209B7), Color(0xFF00FFCC)
        )
        EditorTheme.LIGHT_SAND -> listOf(
            Color(0xFFFCFAF2), Color(0xFF2C2518), Color(0xFFF4EDE2), Color(0xFF8C8070), Color(0xFF7D5913)
        )
    }

    // Custom monospace font styling with premium look
    val textStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.5f).sp,
        color = editorTextColor
    )

    // Code accessories shortcuts character arrays
    val programmingKeys = listOf(
        "{", "}", "(", ")", "[", "]", "<", ">", "/", "=", "\"", "'", ";", ".", ":", ",", "?", "!", "-", "+"
    )

    // Preset insertions lists based on active file tab
    val tagSnippets = when (activeTab) {
        EditorTab.HTML -> listOf(
            "<div>" to "<div>\n  \n</div>",
            "<p>" to "<p></p>",
            "<h1>" to "<h1></h1>",
            "<button>" to "<button id=\"\">\n  Click\n</button>",
            "class style" to "class=\"\"",
            "<img>" to "<img src=\"\" alt=\"\" />",
            "<canvas>" to "<canvas id=\"myCanvas\"></canvas>",
            "Inline CSS" to "<style>\n  \n</style>",
            "Inline JS" to "<script>\n  \n</script>"
        )
        EditorTab.CSS -> listOf(
            "Flex Center" to "display: flex;\njustify-content: center;\nalign-items: center;",
            "Color Grid" to "display: grid;\ngrid-template-columns: repeat(3, 1fr);\ngap: 10px;",
            "Neon Glow" to "box-shadow: 0 0 15px rgba(0, 210, 255, 0.6);\nborder: 1px solid #00d2ff;",
            "Resp Border" to "border-radius: 12px;\npadding: 1.5rem;",
            "Trans Bounce" to "transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);",
            "Full Radial" to "background: radial-gradient(circle, #2c5364, #203a43, #0f2027);"
        )
        EditorTab.JS -> listOf(
            "Click listener" to "document.getElementById('').addEventListener('click', (e) => {\n  \n});",
            "Console Print" to "console.log('');",
            "Canvas rendering" to "const canvas = document.getElementById('myCanvas');\nconst ctx = canvas.getContext('2d');",
            "Loop interval" to "setInterval(() => {\n  \n}, 1000);",
            "Query Select" to "const elements = document.querySelectorAll('');",
            "Fetch REST" to "fetch('https://api.coincap.io/v2/assets')\n  .then(res => res.json())\n  .then(data => console.log(data));",
            "Reset Console" to "console.clear();"
        )
    }

    Column(modifier = modifier) {
        // Snippet and tool configuration panel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E1E24))
                .padding(vertical = 6.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LazyRow of dynamic tags insertion suggestions
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    FilledTonalButton(
                        onClick = onFormat,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatAlignLeft,
                            contentDescription = "Format",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Format", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                items(tagSnippets) { snippet ->
                    SuggestionChip(
                        onClick = {
                            val currentText = textFieldValueState.text
                            val selection = textFieldValueState.selection
                            val insertion = snippet.second
                            val updatedText = currentText.substring(0, selection.start) + insertion + currentText.substring(selection.end)
                            val nextSelection = TextRange(selection.start + insertion.length)
                            
                            textFieldValueState = TextFieldValue(updatedText, nextSelection)
                            onCodeChange(updatedText)
                        },
                        label = { Text(snippet.first, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF26262F),
                            labelColor = Color(0xFFD0D0DA)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF3B3B4A)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Settings gear button
            FilledIconButton(
                onClick = { showSettingsTray = !showSettingsTray },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (showSettingsTray) MaterialTheme.colorScheme.primary else Color(0xFF2E2E38),
                    contentColor = if (showSettingsTray) Color.White else Color(0xFFDCDCE5)
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Toggle editor preferences",
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Settings tray accordion view sliding animation
        AnimatedVisibility(
            visible = showSettingsTray,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161D))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "EDITOR PREFERENCES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Font Size Slider Controller
                    Column(modifier = Modifier.weight(1.1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TextFields,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Font Scale: ${fontSize.toInt()}sp",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Slider(
                            value = fontSize,
                            onValueChange = { fontSize = it },
                            valueRange = 10f..22f,
                            colors = SliderDefaults.colors(
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                thumbColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.height(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Word wrap controller card toggle
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (wordWrap) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) else Color(0xFF25252D),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { wordWrap = !wordWrap }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WrapText,
                                contentDescription = null,
                                tint = if (wordWrap) MaterialTheme.colorScheme.primary else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (wordWrap) "Wrap Active" else "Wrap Off",
                                color = if (wordWrap) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Horizontal Keyboard Punctuation Bar
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF262630))
                .padding(vertical = 5.dp, horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(programmingKeys) { key ->
                Box(
                    modifier = Modifier
                        .background(Color(0xFF383845), shape = RoundedCornerShape(6.dp))
                        .clip(RoundedCornerShape(6.dp))
                        .width(36.dp)
                        .height(32.dp)
                        .clickable {
                            val currentText = textFieldValueState.text
                            val selection = textFieldValueState.selection
                            val updatedText = currentText.substring(0, selection.start) + key + currentText.substring(selection.end)
                            val nextSelection = TextRange(selection.start + key.length)
                            
                            textFieldValueState = TextFieldValue(updatedText, nextSelection)
                            onCodeChange(updatedText)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        color = Color(0xFFF3F3F7),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Shared Scrolling State for gutter and text editor area
        val verticalScrollState = rememberScrollState()
        val horizontalScrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(editorBgColor)
        ) {
            // Line numbers Gutter panel
            val lines = code.lines()
            val totalLines = lines.size.coerceAtLeast(1)
            val lineNumbersString = (1..totalLines).joinToString("\n")

            Column(
                modifier = Modifier
                    .width(42.dp)
                    .fillMaxHeight()
                    .background(editorGutterBg)
                    .verticalScroll(verticalScrollState)
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = lineNumbersString,
                    style = textStyle.copy(
                        color = editorGutterTextColor,
                        fontWeight = FontWeight.Bold
                    ),
                    lineHeight = (fontSize * 1.5f).sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            VerticalDivider(
                color = Color.White.copy(alpha = 0.05f),
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
            )

            // Primary Text Field Editor with scroll bounds
            val contentScrollModifier = if (wordWrap) {
                Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .padding(start = 12.dp, end = 16.dp, top = 8.dp, bottom = 24.dp)
            } else {
                Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScrollState)
                    .horizontalScroll(horizontalScrollState)
                    .padding(start = 12.dp, end = 24.dp, top = 8.dp, bottom = 24.dp)
            }

            Box(modifier = Modifier.fillMaxSize().weight(1f)) {
                BasicTextField(
                    value = textFieldValueState,
                    onValueChange = { newValue ->
                        textFieldValueState = newValue
                        if (newValue.text != code) {
                            onCodeChange(newValue.text)
                        }
                    },
                    textStyle = textStyle,
                    cursorBrush = SolidColor(cursorBrushColor),
                    // HERE IS THE MAGIC SYNTAX HIGHLIGHT INTEGRATION! ⚡
                    visualTransformation = SyntaxHighlightTransformer(activeTab, theme),
                    modifier = contentScrollModifier
                )
            }
        }
    }
}
