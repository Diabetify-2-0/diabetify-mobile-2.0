package com.itb.diabetify.presentation.chatbot

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.itb.diabetify.presentation.chatbot.components.normalizeChatListMarkers
import com.itb.diabetify.presentation.chatbot.components.parseChatMarkdown
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattedChatTextTest {

    private val baseStyle = SpanStyle(color = Color.Black, fontSize = 14.sp)

    @Test
    fun normalizeChatListMarkers_convertsDashAndStarBullets() {
        val input = "- First item\n* Second item\n• Third item"
        val output = normalizeChatListMarkers(input)
        assertEquals("• First item\n• Second item\n• Third item", output)
    }

    @Test
    fun normalizeChatListMarkers_preservesNumberedLists() {
        val input = "1. First\n2. Second"
        assertEquals(input, normalizeChatListMarkers(input))
    }

    @Test
    fun parseChatMarkdown_appliesBoldStyle() {
        val annotated = parseChatMarkdown("This is **important** info.", baseStyle)
        val boldRanges = annotated.spanStyles.filter {
            it.item.fontWeight == FontWeight.Bold
        }
        assertEquals(1, boldRanges.size)
        assertEquals("important", annotated.substring(boldRanges.first().start, boldRanges.first().end))
    }

    @Test
    fun parseChatMarkdown_appliesItalicStyle() {
        val annotated = parseChatMarkdown("Please note *this detail* carefully.", baseStyle)
        val italicRanges = annotated.spanStyles.filter {
            it.item.fontStyle == FontStyle.Italic
        }
        assertEquals(1, italicRanges.size)
        assertEquals("this detail", annotated.substring(italicRanges.first().start, italicRanges.first().end))
    }

    @Test
    fun parseChatMarkdown_leavesUnclosedMarkersLiteralDuringStreaming() {
        val annotated = parseChatMarkdown("Still typing **bold", baseStyle)
        assertTrue(annotated.text.contains("**bold"))
        assertTrue(annotated.spanStyles.none { it.item.fontWeight == FontWeight.Bold })
    }

    @Test
    fun parseChatMarkdown_supportsBoldItalicCombo() {
        val annotated = parseChatMarkdown("***urgent*** action needed", baseStyle)
        val styledRanges = annotated.spanStyles.filter {
            it.item.fontWeight == FontWeight.Bold && it.item.fontStyle == FontStyle.Italic
        }
        assertEquals(1, styledRanges.size)
        assertEquals("urgent", annotated.substring(styledRanges.first().start, styledRanges.first().end))
    }

    @Test
    fun parseChatMarkdown_stripsHeadingMarkersAndAppliesHeadingStyle() {
        val annotated = parseChatMarkdown("### Cara Memantau Tekanan darah", baseStyle)
        assertEquals("Cara Memantau Tekanan darah", annotated.text)
        assertTrue(annotated.text.none { it == '#' })
        assertTrue(
            annotated.spanStyles.any {
                it.item.fontWeight == FontWeight.Bold &&
                    it.item.fontSize.value > baseStyle.fontSize.value
            }
        )
    }

    @Test
    fun parseChatMarkdown_supportsPartialHeadingWhileStreaming() {
        val annotated = parseChatMarkdown("### Cara Memantau", baseStyle)
        assertEquals("Cara Memantau", annotated.text)
        assertTrue(annotated.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
    }
}
