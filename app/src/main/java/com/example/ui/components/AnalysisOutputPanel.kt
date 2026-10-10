package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Download
import androidx.compose.runtime.remember
import com.example.data.model.AlternativeApproach
import com.example.data.model.AnalysisMode
import com.example.data.model.AnalysisRecord
import com.example.data.model.AppLanguage
import com.example.data.model.BugItem
import com.example.data.model.BugSeverity
import com.example.data.model.CodeLanguage
import com.example.data.model.ComparisonRow
import com.example.data.model.DebugResult
import com.example.data.model.DifficultyLevel
import com.example.data.model.ExplainResult
import com.example.data.model.LearningResource
import com.example.data.model.LocalizedStrings
import com.example.data.model.OptimizeResult
import com.example.data.model.Translations
import com.example.data.model.WalkthroughStep
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentRed
import com.example.ui.theme.AccentYellow
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkGreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnalysisOutputPanel(
    isAnalyzing: Boolean,
    analysisProgressText: String,
    streamingText: String = "",
    currentRecord: AnalysisRecord?,
    history: List<AnalysisRecord>,
    errorMessage: String?,
    isApiKeyMissing: Boolean,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    selectedLanguage: CodeLanguage = CodeLanguage.PYTHON,
    selectedMode: AnalysisMode = AnalysisMode.EXPLAIN,
    onSelectHistoryItem: (AnalysisRecord) -> Unit,
    onRetry: () -> Unit,
    onTrySamplePrompt: () -> Unit,
    onExportMarkdown: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(appLanguage) { Translations.get(appLanguage) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top History Chips row (if history exists)
        if (history.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Analysis history",
                    tint = TextMuted,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = strings.historyLabel,
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(history) { record ->
                        val isSelected = currentRecord?.id == record.id
                        val modeLabel = when (record.mode) {
                            AnalysisMode.EXPLAIN -> strings.modeExplain
                            AnalysisMode.DEBUG -> strings.modeDebug
                            AnalysisMode.OPTIMIZE -> strings.modeOptimize
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) PrimaryBlue.copy(alpha = 0.25f) else DarkSurfaceVariant)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) PrimaryBlue else DarkBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable { onSelectHistoryItem(record) }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("history_chip_${record.id.take(4)}")
                        ) {
                            Text(
                                text = "${record.language.iconEmoji} ${record.language.displayName} • $modeLabel",
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // Main Content Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            when {
                // 1. Streaming / Loading State
                isAnalyzing -> {
                    if (streamingText.isBlank()) {
                        // Instant progress spinner when user clicks Analyze
                        LoadingView(progressText = analysisProgressText, strings = strings)
                    } else {
                        // Word-by-word streaming markdown view
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = AccentBlue
                                    )
                                    Column {
                                        Text(
                                            text = "${selectedMode.iconEmoji} ${strings.streamingTitle}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentBlue
                                        )
                                        Text(
                                            text = strings.streamingSubtitle,
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }

                            StreamingMarkdownView(
                                markdown = streamingText,
                                language = selectedLanguage,
                                isStreaming = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 2. Error State
                errorMessage != null -> {
                    ErrorView(
                        errorMessage = errorMessage,
                        isApiKeyMissing = isApiKeyMissing,
                        strings = strings,
                        onRetry = onRetry
                    )
                }

                // 3. Success State with Analysis Result
                currentRecord != null -> {
                    AnalysisResultView(
                        record = currentRecord,
                        appLanguage = appLanguage,
                        strings = strings,
                        onExportMarkdown = onExportMarkdown
                    )
                }

                // 4. Empty State before first analysis
                else -> {
                    EmptyStateView(
                        onTrySamplePrompt = onTrySamplePrompt,
                        strings = strings
                    )
                }
            }
        }
    }
}

@Composable
private fun LoadingView(progressText: String, strings: LocalizedStrings) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(2.dp, DarkBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                color = PrimaryBlue,
                strokeWidth = 3.dp,
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = strings.analyzingButton,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = progressText,
            fontSize = 13.sp,
            color = AccentBlue,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ErrorView(
    errorMessage: String,
    isApiKeyMissing: Boolean,
    strings: LocalizedStrings,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = AccentRed,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.errorTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 18.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        if (isApiKeyMissing) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.padding(horizontal = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "To fix this:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentYellow
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "1. Open the Secrets panel in AI Studio UI.\n2. Add GEMINI_API_KEY with your Google AI Studio API key.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier.testTag("retry_analysis_button")
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.retry)
        }
    }
}

@Composable
private fun EmptyStateView(
    onTrySamplePrompt: () -> Unit,
    strings: LocalizedStrings
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .clip(CircleShape)
                .background(DarkSurfaceVariant)
                .border(1.dp, DarkBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔍",
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.emptyTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = strings.emptyDescription,
            fontSize = 13.sp,
            color = TextSecondary,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Feature highlight tags
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgeTag("📖 ${strings.modeExplain}", AccentBlue)
            BadgeTag("🐞 ${strings.modeDebug}", AccentRed)
            BadgeTag("⚡ ${strings.modeOptimize}", AccentGreen)
        }

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = onTrySamplePrompt,
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = DarkSurfaceVariant.copy(alpha = 0.5f),
                contentColor = TextPrimary
            ),
            modifier = Modifier.testTag("empty_state_sample_button")
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = AccentYellow,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(strings.loadSampleAction)
        }
    }
}

@Composable
private fun BadgeTag(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun DifficultyRatingChip(
    difficulty: DifficultyLevel,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val color = Color(difficulty.badgeColorHex)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .testTag("difficulty_rating_chip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(text = difficulty.emoji, fontSize = 12.sp)
            Text(
                text = difficulty.label(appLanguage),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun AnalysisResultView(
    record: AnalysisRecord,
    appLanguage: AppLanguage,
    strings: LocalizedStrings,
    onExportMarkdown: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Result Summary Header Banner with Difficulty Rating & Download Button
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = record.mode.iconEmoji,
                                fontSize = 16.sp
                            )
                            val modeLabel = when (record.mode) {
                                AnalysisMode.EXPLAIN -> strings.modeExplain
                                AnalysisMode.DEBUG -> strings.modeDebug
                                AnalysisMode.OPTIMIZE -> strings.modeOptimize
                            }
                            Text(
                                text = modeLabel,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            // Difficulty Rating chip in Explain mode banner
                            if (record.mode == AnalysisMode.EXPLAIN && record.explainResult != null) {
                                DifficultyRatingChip(
                                    difficulty = record.explainResult.difficulty,
                                    appLanguage = appLanguage
                                )
                            }
                        }
                        Text(
                            text = "${record.language.displayName} • AI Verified",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Download Markdown Button
                    OutlinedButton(
                        onClick = onExportMarkdown,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PrimaryBlue),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = PrimaryBlue.copy(alpha = 0.15f),
                            contentColor = AccentBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("download_markdown_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download Markdown",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = strings.downloadMarkdown,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Mode specific views
        when (record.mode) {
            AnalysisMode.EXPLAIN -> {
                record.explainResult?.let { result ->
                    // 1. Overview with Difficulty Rating
                    item {
                        CollapsibleSection(
                            title = strings.overviewTitle,
                            badgeText = result.difficulty.label(appLanguage),
                            badgeTextColor = Color(result.difficulty.badgeColorHex),
                            icon = {
                                Icon(Icons.Default.Description, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${strings.difficultyTitle}:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextSecondary
                                    )
                                    DifficultyRatingChip(
                                        difficulty = result.difficulty,
                                        appLanguage = appLanguage
                                    )
                                }

                                Text(
                                    text = result.overview,
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // 2. Line-by-Line Walkthrough
                    item {
                        CollapsibleSection(
                            title = strings.walkthroughTitle,
                            badgeText = "${result.walkthrough.size} sections",
                            badgeTextColor = AccentYellow,
                            icon = {
                                Icon(Icons.Default.Code, null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                result.walkthrough.forEach { step ->
                                    WalkthroughCard(step)
                                }
                            }
                        }
                    }

                    // 3. Key Concepts
                    item {
                        CollapsibleSection(
                            title = strings.conceptsTitle,
                            badgeText = "${result.keyConcepts.size} concepts",
                            badgeTextColor = AccentPurple,
                            icon = {
                                Icon(Icons.Default.Lightbulb, null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Data structures, algorithms, and design patterns utilized in this code:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    result.keyConcepts.forEach { concept ->
                                        BadgeTag(concept, AccentPurple)
                                    }
                                }
                            }
                        }
                    }

                    // 4. Complexity Analysis
                    item {
                        CollapsibleSection(
                            title = strings.complexityTitle,
                            badgeText = "Big O",
                            badgeTextColor = AccentGreen,
                            icon = {
                                Icon(Icons.Default.Speed, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                ComplexityCard(
                                    type = "Time Complexity",
                                    bigO = result.timeComplexity,
                                    justification = result.timeJustification,
                                    accentColor = AccentGreen
                                )
                                ComplexityCard(
                                    type = "Space Complexity",
                                    bigO = result.spaceComplexity,
                                    justification = result.spaceJustification,
                                    accentColor = AccentBlue
                                )
                            }
                        }
                    }

                    // 5. Learning Resources
                    item {
                        CollapsibleSection(
                            title = strings.resourcesTitle,
                            badgeText = "Study",
                            badgeTextColor = AccentBlue,
                            icon = {
                                Icon(Icons.Default.School, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                result.learningResources.forEach { res ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = DarkSurfaceVariant,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = res.topic,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = AccentBlue
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = res.description,
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnalysisMode.DEBUG -> {
                record.debugResult?.let { result ->
                    // 1. Bugs Found
                    item {
                        CollapsibleSection(
                            title = strings.bugsTitle,
                            badgeText = "${result.bugs.size} detected",
                            badgeTextColor = if (result.bugs.any { it.severity == BugSeverity.CRITICAL }) AccentRed else AccentYellow,
                            icon = {
                                Icon(Icons.Default.BugReport, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            if (result.bugs.isEmpty()) {
                                Text(
                                    text = "✅ No explicit bugs identified in this snippet.",
                                    fontSize = 13.sp,
                                    color = AccentGreen
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    result.bugs.forEachIndexed { index, bug ->
                                        BugItemCard(index = index + 1, bug = bug)
                                    }
                                }
                            }
                        }
                    }

                    // 2. Fixed Code
                    item {
                        CollapsibleSection(
                            title = strings.fixedCodeTitle,
                            badgeText = "Ready to run",
                            badgeTextColor = AccentGreen,
                            icon = {
                                Icon(Icons.Default.Code, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Complete corrected code ready to copy-paste:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                CodeBlockView(
                                    code = result.fixedCode,
                                    language = record.language,
                                    title = "fixed_${record.language.displayName.lowercase()}.${record.language.fileExtension}"
                                )
                            }
                        }
                    }

                    // 3. Edge Cases
                    item {
                        CollapsibleSection(
                            title = strings.edgeCasesTitle,
                            badgeText = "${result.edgeCases.size} cases",
                            badgeTextColor = AccentYellow,
                            icon = {
                                Icon(Icons.Default.Warning, null, tint = AccentYellow, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Situations and inputs that break the original code:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                result.edgeCases.forEach { edgeCase ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "•", color = AccentYellow, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = edgeCase,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Best Practice Notes
                    item {
                        CollapsibleSection(
                            title = strings.bestPracticesTitle,
                            badgeText = "Conventions",
                            badgeTextColor = AccentBlue,
                            icon = {
                                Icon(Icons.Default.School, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                result.bestPractices.forEach { note ->
                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = "✓", color = AccentGreen, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = note,
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = TextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            AnalysisMode.OPTIMIZE -> {
                record.optimizeResult?.let { result ->
                    // 1. Bottlenecks
                    item {
                        CollapsibleSection(
                            title = strings.bottlenecksTitle,
                            badgeText = result.currentComplexity,
                            badgeTextColor = AccentRed,
                            icon = {
                                Icon(Icons.Default.Warning, null, tint = AccentRed, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = result.bottlenecks,
                                    fontSize = 13.sp,
                                    lineHeight = 20.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }

                    // 2. Optimized Code
                    item {
                        CollapsibleSection(
                            title = strings.optimizedCodeTitle,
                            badgeText = "Faster",
                            badgeTextColor = AccentGreen,
                            icon = {
                                Icon(Icons.Default.Speed, null, tint = AccentGreen, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "High-performance version with improved time & space efficiency:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                CodeBlockView(
                                    code = result.optimizedCode,
                                    language = record.language,
                                    title = "optimized_${record.language.displayName.lowercase()}.${record.language.fileExtension}"
                                )
                            }
                        }
                    }

                    // 3. Before vs After
                    item {
                        CollapsibleSection(
                            title = strings.comparisonTitle,
                            badgeText = "Comparison",
                            badgeTextColor = AccentBlue,
                            icon = {
                                Icon(Icons.AutoMirrored.Filled.CompareArrows, null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                            }
                        ) {
                            ComparisonTableView(rows = result.comparisonTable)
                        }
                    }

                    // 4. Alternative Approach
                    result.alternativeApproach?.let { alt ->
                        item {
                            CollapsibleSection(
                                title = "${strings.alternativeTitle}: ${alt.title}",
                                badgeText = "Alternative",
                                badgeTextColor = AccentPurple,
                                icon = {
                                    Icon(Icons.Default.Lightbulb, null, tint = AccentPurple, modifier = Modifier.size(18.dp))
                                }
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = alt.explanation,
                                        fontSize = 13.sp,
                                        lineHeight = 20.sp,
                                        color = TextPrimary
                                    )
                                    if (alt.codeSnippet.isNotBlank()) {
                                        CodeBlockView(
                                            code = alt.codeSnippet,
                                            language = record.language,
                                            title = "alternative_snippet"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Export Report Banner
        item {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = DarkSurfaceVariant.copy(alpha = 0.5f),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = strings.downloadMarkdown,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Save complete analysis report as formatted .md file",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                    Button(
                        onClick = onExportMarkdown,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("download_bottom_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Export .md")
                    }
                }
            }
        }
    }
}

@Composable
private fun WalkthroughCard(step: WalkthroughStep) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(PrimaryBlue.copy(alpha = 0.2f))
                    .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = step.lineRange,
                    fontFamily = JetBrainsMono,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step.explanation,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ComplexityCard(
    type: String,
    bigO: String,
    justification: String,
    accentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.2f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = bigO,
                    fontFamily = JetBrainsMono,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = justification,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun BugItemCard(index: Int, bug: BugItem) {
    val isCritical = bug.severity == BugSeverity.CRITICAL
    val badgeBg = if (isCritical) AccentRed.copy(alpha = 0.2f) else AccentYellow.copy(alpha = 0.2f)
    val badgeBorder = if (isCritical) AccentRed.copy(alpha = 0.5f) else AccentYellow.copy(alpha = 0.5f)
    val badgeTextColor = if (isCritical) AccentRed else AccentYellow

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "#$index",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeBg)
                            .border(1.dp, badgeBorder, RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "${bug.severity.icon} ${bug.severity.label}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeTextColor
                        )
                    }
                }

                if (bug.lineNumber.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DarkBg)
                            .border(1.dp, DarkBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Line ${bug.lineNumber}",
                            fontFamily = JetBrainsMono,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = bug.whatIsWrong,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Why it fails: ${bug.whyItFails}",
                fontSize = 12.sp,
                lineHeight = 18.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ComparisonTableView(rows: List<ComparisonRow>) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = DarkBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "METRIC",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    modifier = Modifier.weight(0.4f)
                )
                Text(
                    text = "BEFORE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentRed,
                    modifier = Modifier.weight(0.3f)
                )
                Text(
                    text = "AFTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentGreen,
                    modifier = Modifier.weight(0.3f)
                )
            }

            // Data Rows
            rows.forEachIndexed { idx, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (idx % 2 == 1) DarkSurfaceVariant.copy(alpha = 0.3f) else Color.Transparent)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.metric,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary,
                        modifier = Modifier.weight(0.4f)
                    )
                    Text(
                        text = row.before,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        color = AccentRed.copy(alpha = 0.9f),
                        modifier = Modifier.weight(0.3f)
                    )
                    Text(
                        text = row.after,
                        fontFamily = JetBrainsMono,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.weight(0.3f)
                    )
                }
                if (idx < rows.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(DarkBorderSubtle)
                    )
                }
            }
        }
    }
}
