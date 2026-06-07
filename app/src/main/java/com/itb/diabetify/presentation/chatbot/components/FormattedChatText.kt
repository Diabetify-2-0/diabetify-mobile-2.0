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
import androidx.compose.ui.unit.sp

private val bulletLinePattern = Regex("""^(\s*)[-*•]\s+(.+)$""")
private val headingLinePattern = Regex("""^(#{1,6})\s*(.*)$""")

/**
 * Normalizes list markers so bullet lines render consistently in chat bubbles.
 */
internal fun normalizeChatListMarkers(text: String): String {
    if (text.isEmpty()) return text
    return text.lines().joinToString("\n") { line ->
        normalizeBulletLine(line)
    }
}

private fun normalizeBulletLine(line: String): String {
    val match = bulletLinePattern.matchEntire(line)
    return if (match != null) {
        "${match.groupValues[1]}• ${match.groupValues[2]}"
    } else {
        line
    }
}

private fun headingStyle(baseStyle: SpanStyle, level: Int): SpanStyle {
    val sizeMultiplier = when (level) {
        1 -> 1.28f
        2 -> 1.2f
        3 -> 1.14f
        4 -> 1.1f
        else -> 1.06f
    }
    val baseSize = baseStyle.fontSize
    val scaledSize = if (baseSize != TextUnit.Unspecified) {
        (baseSize.value * sizeMultiplier).sp
    } else {
        TextUnit.Unspecified
    }
    return baseStyle.copy(
        fontWeight = FontWeight.Bold,
        fontSize = scaledSize,
    )
}

/**
 * Parses a small subset of Markdown used in chatbot replies:
 * - # headings (h1–h6)
 * - **bold**
 * - *italic*
 * - _italic_
 * - bullet lines (-, *, •)
 * - numbered lists (1. item)
 *
 * Unclosed markers are shown literally so streaming tokens stay readable.
 */
internal fun parseChatMarkdown(text: String, baseStyle: SpanStyle): AnnotatedString {
    if (text.isEmpty()) return AnnotatedString("")

    return buildAnnotatedString {
        val lines = text.split('\n')
        lines.forEachIndexed { lineIndex, rawLine ->
            if (lineIndex > 0) {
                append('\n')
            }

            val headingMatch = headingLinePattern.matchEntire(rawLine)
            if (headingMatch != null) {
                val level = headingMatch.groupValues[1].length
                val headingContent = headingMatch.groupValues[2]
                appendInlineMarkdown(
                    text = headingContent,
                    lineStyle = headingStyle(baseStyle, level),
                )
            } else {
                appendInlineMarkdown(
                    text = normalizeBulletLine(rawLine),
                    lineStyle = baseStyle,
                )
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(
    text: String,
    lineStyle: SpanStyle,
) {
    withStyle(lineStyle) {
        var index = 0
        while (index < text.length) {
            when {
                text.startsWith("***", index) -> {
                    val end = text.indexOf("***", index + 3)
                    if (end != -1) {
                        withStyle(
                            lineStyle.copy(
                                fontWeight = FontWeight.Bold,
                                fontStyle = FontStyle.Italic,
                            )
                        ) {
                            append(text.substring(index + 3, end))
                        }
                        index = end + 3
                    } else {
                        append("***")
                        index += 3
                    }
                }

                text.startsWith("**", index) -> {
                    val end = text.indexOf("**", index + 2)
                    if (end != -1) {
                        withStyle(lineStyle.copy(fontWeight = FontWeight.Bold)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                    } else {
                        append("**")
                        index += 2
                    }
                }

                text.startsWith("__", index) -> {
                    val end = text.indexOf("__", index + 2)
                    if (end != -1) {
                        withStyle(lineStyle.copy(fontWeight = FontWeight.Bold)) {
                            append(text.substring(index + 2, end))
                        }
                        index = end + 2
                    } else {
                        append("__")
                        index += 2
                    }
                }

                text[index] == '*' && !isEscaped(text, index) -> {
                    val end = findClosingDelimiter(text, index + 1, '*')
                    if (end != -1) {
                        withStyle(lineStyle.copy(fontStyle = FontStyle.Italic)) {
                            append(text.substring(index + 1, end))
                        }
                        index = end + 1
                    } else {
                        append('*')
                        index += 1
                    }
                }

                text[index] == '_' && !isEscaped(text, index) -> {
                    val end = findClosingDelimiter(text, index + 1, '_')
                    if (end != -1) {
                        withStyle(lineStyle.copy(fontStyle = FontStyle.Italic)) {
                            append(text.substring(index + 1, end))
                        }
                        index = end + 1
                    } else {
                        append('_')
                        index += 1
                    }
                }

                else -> {
                    append(text[index])
                    index += 1
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
