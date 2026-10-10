package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CodeLanguage
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class Code(val language: String, val code: String) : MarkdownBlock()
    data class Bullet(val text: String) : MarkdownBlock()
    data class Table(val headers: List<String>, val rows: List<List<String>>) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

@Composable
fun StreamingMarkdownView(
    markdown: String,
    language: CodeLanguage,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val blocks = remember(markdown) {
        parseMarkdownBlocks(markdown)
    }

    // Auto-scroll to bottom as chunks stream in
    LaunchedEffect(blocks.size, markdown.length) {
        if (isStreaming && blocks.isNotEmpty()) {
            listState.animateScrollToItem(blocks.size - 1)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursorBlink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(blocks) { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val color = when {
                        block.text.contains("1") || block.text.contains("Overview") -> AccentBlue
                        block.text.contains("2") || block.text.contains("Walkthrough") || block.text.contains("Bug") -> AccentYellow
                        block.text.contains("Fixed") || block.text.contains("Optimized") -> AccentGreen
                        block.text.contains("3") || block.text.contains("Concept") -> AccentPurple
                        block.text.contains("4") || block.text.contains("Complexity") || block.text.contains("Bottleneck") -> AccentRed
                        else -> AccentBlue
                    }
                    Column(modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 3.dp, height = if (block.level == 2) 18.dp else 14.dp)
                                    .background(color, RoundedCornerShape(2.dp))
                            )
                            Text(
                                text = block.text,
                                fontSize = if (block.level == 2) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }

                is MarkdownBlock.Code -> {
                    CodeBlockView(
                        code = block.code,
                        language = language,
                        title = "${language.displayName.lowercase()}_code"
                    )
                }

                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            color = AccentBlue,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Text(
                            text = block.text,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = TextPrimary
                        )
                    }
                }

                is MarkdownBlock.Table -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = DarkBg,
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DarkSurfaceVariant)
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                block.headers.forEachIndexed { i, h ->
                                    Text(
                                        text = h,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (i == 1) AccentRed else if (i == 2) AccentGreen else TextMuted,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                            // Rows
                            block.rows.forEachIndexed { idx, row ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(if (idx % 2 == 1) DarkSurfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    row.forEachIndexed { i, cell ->
                                        Text(
                                            text = cell,
                                            fontFamily = if (i > 0) JetBrainsMono else null,
                                            fontSize = 12.sp,
                                            fontWeight = if (i == 2) FontWeight.Bold else FontWeight.Normal,
                                            color = if (i == 1) AccentRed.copy(alpha = 0.9f) else if (i == 2) AccentGreen else TextPrimary,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = block.text,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // Live blinking cursor at bottom during streaming
        if (isStreaming) {
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 16.dp)
                            .alpha(cursorAlpha)
                            .background(AccentGreen)
                    )
                    Text(
                        text = "streaming...",
                        fontSize = 11.sp,
                        fontFamily = JetBrainsMono,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

internal fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    if (markdown.isBlank()) return emptyList()
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var index = 0

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()
        val prevIndex = index

        when {
            // Code block start
            trimmed.startsWith("```") -> {
                val langTag = trimmed.removePrefix("```").trim()
                val codeLines = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    codeLines.add(lines[index])
                    index++
                }
                // Even if closing ``` hasn't arrived yet (while streaming), include the code!
                blocks.add(MarkdownBlock.Code(langTag, codeLines.joinToString("\n")))
                if (index < lines.size && lines[index].trim().startsWith("```")) {
                    index++
                }
            }

            // Heading: #, ##, ###, etc.
            trimmed.startsWith("#") -> {
                val level = trimmed.takeWhile { it == '#' }.length
                val headerText = trimmed.drop(level).trim()
                blocks.add(MarkdownBlock.Header(level, headerText))
                index++
            }

            // Bullet or numbered list
            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") || trimmed.startsWith("• ") ||
            Regex("""^\d+\.\s+""").containsMatchIn(trimmed) -> {
                val bulletText = trimmed
                    .replace(Regex("""^([*\-+•]|\d+\.)\s*"""), "")
                    .trim()
                blocks.add(MarkdownBlock.Bullet(bulletText))
                index++
            }

            // Table row
            trimmed.startsWith("|") -> {
                val headers = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                val tableRows = mutableListOf<List<String>>()
                index++
                // Skip separator if present
                if (index < lines.size && lines[index].contains("---")) {
                    index++
                }
                while (index < lines.size && lines[index].trim().startsWith("|")) {
                    val row = lines[index].split("|").map { it.trim() }.filter { it.isNotEmpty() }
                    if (row.isNotEmpty()) tableRows.add(row)
                    index++
                }
                if (headers.isNotEmpty() || tableRows.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Table(headers, tableRows))
                }
            }

            // Empty line
            trimmed.isEmpty() -> {
                index++
            }

            // Regular paragraph
            else -> {
                val paraLines = mutableListOf<String>()
                while (index < lines.size) {
                    val curTrimmed = lines[index].trim()
                    if (curTrimmed.isEmpty() ||
                        curTrimmed.startsWith("#") ||
                        curTrimmed.startsWith("```") ||
                        curTrimmed.startsWith("- ") ||
                        curTrimmed.startsWith("* ") ||
                        curTrimmed.startsWith("+ ") ||
                        curTrimmed.startsWith("• ") ||
                        curTrimmed.startsWith("|") ||
                        Regex("""^\d+\.\s+""").containsMatchIn(curTrimmed)
                    ) {
                        break
                    }
                    paraLines.add(lines[index])
                    index++
                }
                if (index == prevIndex) {
                    paraLines.add(lines[index])
                    index++
                }
                val text = paraLines.joinToString(" ").trim()
                if (text.isNotEmpty()) {
                    blocks.add(MarkdownBlock.Paragraph(text))
                }
            }
        }

        // Guarantees index advances by at least 1 on every iteration
        if (index <= prevIndex) {
            index = prevIndex + 1
        }
    }

    return blocks
}
