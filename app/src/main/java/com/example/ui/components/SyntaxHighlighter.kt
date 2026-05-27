package com.example.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import com.example.ui.EditorTab
import com.example.ui.EditorTheme
import java.util.regex.Pattern

data class HighlightColors(
    val defaultText: Color,
    val keyword: Color,     // const, function
    val tag: Color,         // <div, </div>
    val attrName: Color,    // class, id, style
    val attrValue: Color,   // "container", "btn"
    val comment: Color,     // // or <!-- -->
    val number: Color,      // numbers, css values
    val symbol: Color       // brackets, braces
) {
    companion object {
        fun getColors(theme: EditorTheme): HighlightColors {
            return when (theme) {
                EditorTheme.DARK_MONOCHROME -> HighlightColors(
                    defaultText = Color(0xFFE4E4EB),
                    keyword = Color(0xFFA8A8B2),
                    tag = Color(0xFFF3F3F5),
                    attrName = Color(0xFF9EA3B0),
                    attrValue = Color(0xFF86B0AC),
                    comment = Color(0xFF6F6F76),
                    number = Color(0xFFFFB86C),
                    symbol = Color(0xFFBBBCC4)
                )
                EditorTheme.OBSIDIAN_DRACULA -> HighlightColors(
                    defaultText = Color(0xFFF8F8F2),
                    keyword = Color(0xFFBD93F9),
                    tag = Color(0xFFFF79C6),
                    attrName = Color(0xFF50FA7B),
                    attrValue = Color(0xFFF1FA8C),
                    comment = Color(0xFF6272A4),
                    number = Color(0xFFFFB86C),
                    symbol = Color(0xFF8BE9FD)
                )
                EditorTheme.COBALT_DEEP -> HighlightColors(
                    defaultText = Color(0xFFE0F7FA),
                    keyword = Color(0xFFFFC107),
                    tag = Color(0xFF80DEEA),
                    attrName = Color(0xFF40C4FF),
                    attrValue = Color(0xFF69F0AE),
                    comment = Color(0xFF78909C),
                    number = Color(0xFFFF5252),
                    symbol = Color(0xFFFFFFFF)
                )
                EditorTheme.CYBERPUNK -> HighlightColors(
                    defaultText = Color(0xFFE0E0FF),
                    keyword = Color(0xFFBD00FF),
                    tag = Color(0xFF00FFCC),
                    attrName = Color(0xFFFF007F),
                    attrValue = Color(0xFFFFF500),
                    comment = Color(0xFF7209B7),
                    number = Color(0xFF00FFFF),
                    symbol = Color(0xFFFFFFFF)
                )
                EditorTheme.LIGHT_SAND -> HighlightColors(
                    defaultText = Color(0xFF2C2518),
                    keyword = Color(0xFF7D5913),
                    tag = Color(0xFFB45309),
                    attrName = Color(0xFF0D9488),
                    attrValue = Color(0xFF15803D),
                    comment = Color(0xFF8C8070),
                    number = Color(0xFF0369A1),
                    symbol = Color(0xFF451A03)
                )
            }
        }
    }
}

class SyntaxHighlightTransformer(
    private val tab: EditorTab,
    private val theme: EditorTheme
) : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val highlighted = highlight(text.text, tab, theme)
        return TransformedText(highlighted, OffsetMapping.Identity)
    }

    private fun highlight(code: String, tab: EditorTab, theme: EditorTheme): AnnotatedString {
        val colors = HighlightColors.getColors(theme)
        val builder = AnnotatedString.Builder(code)

        try {
            when (tab) {
                EditorTab.HTML -> {
                    // 1. Comments: <!-- ... -->
                    val commentPattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL)
                    val commentMatcher = commentPattern.matcher(code)
                    while (commentMatcher.find()) {
                        builder.addStyle(
                            SpanStyle(color = colors.comment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            commentMatcher.start(),
                            commentMatcher.end()
                        )
                    }

                    // 2. Tag names and attributes
                    val tagPattern = Pattern.compile("</?[a-zA-Z0-9:-]+|/?>|(\\s[a-zA-Z0-9:-]+(?=\\s*=))")
                    val tagMatcher = tagPattern.matcher(code)
                    while (tagMatcher.find()) {
                        val token = tagMatcher.group()
                        val start = tagMatcher.start()
                        val end = tagMatcher.end()
                        if (token.startsWith("<") || token.endsWith(">")) {
                            builder.addStyle(SpanStyle(color = colors.tag, fontWeight = FontWeight.Bold), start, end)
                        } else {
                            builder.addStyle(SpanStyle(color = colors.attrName), start, end)
                        }
                    }

                    // 3. String values: "..." or '...'
                    val stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'")
                    val stringMatcher = stringPattern.matcher(code)
                    while (stringMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.attrValue), stringMatcher.start(), stringMatcher.end())
                    }
                }
                EditorTab.CSS -> {
                    // 1. Comments: /* ... */
                    val commentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)
                    val commentMatcher = commentPattern.matcher(code)
                    while (commentMatcher.find()) {
                        builder.addStyle(
                            SpanStyle(color = colors.comment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                            commentMatcher.start(),
                            commentMatcher.end()
                        )
                    }

                    // 2. Symbols: { } ; :
                    val bracePattern = Pattern.compile("([{};:])")
                    val braceMatcher = bracePattern.matcher(code)
                    while (braceMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.symbol, fontWeight = FontWeight.Bold), braceMatcher.start(), braceMatcher.end())
                    }

                    // 3. CSS property keys (e.g., color, margin)
                    val propPattern = Pattern.compile("(?<=[\\{\\n;\\s])[a-zA-Z- ]+(?=\\s*:[^;]+)")
                    val propMatcher = propPattern.matcher(code)
                    while (propMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.attrName), propMatcher.start(), propMatcher.end())
                    }

                    // 4. CSS values
                    val valPattern = Pattern.compile("(?<=:)[^;\\}]+(?=[;\\}])")
                    val valMatcher = valPattern.matcher(code)
                    while (valMatcher.find()) {
                        val start = valMatcher.start()
                        val end = valMatcher.end()
                        builder.addStyle(SpanStyle(color = colors.attrValue), start, end)
                    }

                    // 5. Numbers inside values
                    val numPattern = Pattern.compile("\\b(\\d+\\.?\\d*)(px|rem|em|vh|vw|deg|%)?\\b")
                    val numMatcher = numPattern.matcher(code)
                    while (numMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.number, fontWeight = FontWeight.SemiBold), numMatcher.start(), numMatcher.end())
                    }
                }
                EditorTab.JS -> {
                    // 1. Comments
                    val lineCommentPattern = Pattern.compile("//.*")
                    val lineCommentMatcher = lineCommentPattern.matcher(code)
                    while (lineCommentMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.comment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), lineCommentMatcher.start(), lineCommentMatcher.end())
                    }

                    val blockCommentPattern = Pattern.compile("/\\*.*?\\*/", Pattern.DOTALL)
                    val blockCommentMatcher = blockCommentPattern.matcher(code)
                    while (blockCommentMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.comment, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic), blockCommentMatcher.start(), blockCommentMatcher.end())
                    }

                    // 2. Keywords
                    val keywords = listOf(
                        "const", "let", "var", "function", "return", "if", "else", "for", "while",
                        "do", "switch", "case", "break", "continue", "class", "export", "import", "from",
                        "new", "this", "true", "false", "null", "undefined", "try", "catch", "throw", "async", "await"
                    )
                    val kwPattern = Pattern.compile("\\b(" + keywords.joinToString("|") + ")\\b")
                    val kwMatcher = kwPattern.matcher(code)
                    while (kwMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.keyword, fontWeight = FontWeight.Bold), kwMatcher.start(), kwMatcher.end())
                    }

                    // 3. String literals: "...", '...', or `...`
                    val stringPattern = Pattern.compile("\"[^\"]*\"|'[^']*'|`[^`]*`")
                    val stringMatcher = stringPattern.matcher(code)
                    while (stringMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.attrValue), stringMatcher.start(), stringMatcher.end())
                    }

                    // 4. Numbers
                    val numPattern = Pattern.compile("\\b(0x[0-9a-fA-F]+|\\d+\\.?\\d*)\\b")
                    val numMatcher = numPattern.matcher(code)
                    while (numMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.number), numMatcher.start(), numMatcher.end())
                    }

                    // 5. Brackets / Operators
                    val opPattern = Pattern.compile("([\\{\\}\\(\\)\\[\\]\\+\\-\\*/=<>!&\\|\\?\\.:,;])")
                    val opMatcher = opPattern.matcher(code)
                    while (opMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.symbol), opMatcher.start(), opMatcher.end())
                    }

                    // 6. Highlight common APIs
                    val apiPattern = Pattern.compile("\\b(document|window|console|addEventListener|getElementById|querySelector|querySelectorAll|setTimeout|setInterval|fetch|JSON)\\b")
                    val apiMatcher = apiPattern.matcher(code)
                    while (apiMatcher.find()) {
                        builder.addStyle(SpanStyle(color = colors.tag, fontWeight = FontWeight.Bold), apiMatcher.start(), apiMatcher.end())
                    }
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        return builder.toAnnotatedString()
    }
}
