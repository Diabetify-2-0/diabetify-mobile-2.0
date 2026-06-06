package com.itb.diabetify.presentation.chatbot.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit

private val bulletLinePattern = Regex("""^(\s*)[-*•]\s+(.+)$""")

/**
 * Normalizes list markers so bullet lines render consistently in chat bubbles.
 */
internal fun normalizeChatListMarkers(text: String): String {
    if (text.isEmpty()) return text
    return text.lines().joinToString("\n") { line ->
        val match = bulletLinePattern.matchEntire(line)
        if (match != null) {
            "${match.groupValues[1]}• ${match.groupValues[2]}"
        } else {
            line
        }
    }
}

/**
 * Parses a small subset of Markdown used in chatbot replies:
 * - **bold**
 * - *italic*
 * - _italic_
 * - bullet lines (-, *, •)
 * - numbered lists (1. item)
 *
 * Unclosed markers are shown literally so streaming tokens stay readable.
 */
internal fun parseChatMarkdown(text: String, baseStyle: SpanStyle): AnnotatedString {
    val normalized = normalizeChatListMarkers(text)
    return buildAnnotatedString {
        withStyle(baseStyle) {
            var index = 0
            while (index < normalized.length) {
                when {
                    normalized.startsWith("***", index) -> {
                        val end = normalized.indexOf("***", index + 3)
                        if (end != -1) {
                            withStyle(
                                baseStyle.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                )
                            ) {
                                append(normalized.substring(index + 3, end))
                            }
                            index = end + 3
                        } else {
                            append("***")
                            index += 3
                        }
                    }

                    normalized.startsWith("**", index) -> {
                        val end = normalized.indexOf("**", index + 2)
                        if (end != -1) {
                            withStyle(baseStyle.copy(fontWeight = FontWeight.Bold)) {
                                append(normalized.substring(index + 2, end))
                            }
                            index = end + 2
                        } else {
                            append("**")
                            index += 2
                        }
                    }

                    normalized.startsWith("__", index) -> {
                        val end = normalized.indexOf("__", index + 2)
                        if (end != -1) {
                            withStyle(baseStyle.copy(fontWeight = FontWeight.Bold)) {
                                append(normalized.substring(index + 2, end))
                            }
                            index = end + 2
                        } else {
                            append("__")
                            index += 2
                        }
                    }

                    normalized[index] == '*' && !isEscaped(normalized, index) -> {
                        val end = findClosingDelimiter(normalized, index + 1, '*')
                        if (end != -1) {
                            withStyle(baseStyle.copy(fontStyle = FontStyle.Italic)) {
                                append(normalized.substring(index + 1, end))
                            }
                            index = end + 1
                        } else {
                            append('*')
                            index += 1
                        }
                    }

                    normalized[index] == '_' && !isEscaped(normalized, index) -> {
                        val end = findClosingDelimiter(normalized, index + 1, '_')
                        if (end != -1) {
                            withStyle(baseStyle.copy(fontStyle = FontStyle.Italic)) {
                                append(normalized.substring(index + 1, end))
                            }
                            index = end + 1
                        } else {
                            append('_')
                            index += 1
                        }
                    }

                    else -> {
                        append(normalized[index])
                        index += 1
                    }
                }
            }
        }
    }
}

private fun isEscaped(text: String, index: Int): Boolean {
    var backslashes = 0
    var cursor = index - 1
    while (cursor >= 0 && text[cursor] == '\\') {
        backslashes += 1
        cursor -= 1
    }
    return backslashes % 2 == 1
}

private fun findClosingDelimiter(text: String, start: Int, delimiter: Char): Int {
    var cursor = start
    while (cursor < text.length) {
        if (text[cursor] == delimiter && !isEscaped(text, cursor)) {
            return cursor
        }
        cursor += 1
    }
    return -1
}

@Composable
fun FormattedChatText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    lineHeight: TextUnit,
) {
    val annotatedText = remember(text, color, fontFamily, fontSize) {
        parseChatMarkdown(
            text = text,
            baseStyle = SpanStyle(
                color = color,
                fontFamily = fontFamily,
                fontSize = fontSize,
            ),
        )
    }

    Text(
        text = annotatedText,
        modifier = modifier,
        lineHeight = lineHeight,
    )
}
