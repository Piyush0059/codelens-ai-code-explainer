package com.example.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.LocalizedStrings
import com.example.ui.components.AnalysisOutputPanel
import com.example.ui.components.CodeInputPanel
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkBorderSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.JetBrainsMono
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class MobileTab {
    EDITOR,
    ANALYSIS
}

@Composable
fun MainScreen(
    viewModel: CodeLensViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mobileSelectedTab by remember { mutableStateOf(MobileTab.EDITOR) }

    // Auto-switch to Analysis tab on mobile when analysis begins
    LaunchedEffect(uiState.isAnalyzing) {
        if (uiState.isAnalyzing) {
            mobileSelectedTab = MobileTab.ANALYSIS
        }
    }

    Scaffold(
        containerColor = DarkBg,
        topBar = {
            HeaderBar(
                isAnalyzing = uiState.isAnalyzing,
                resultReady = uiState.currentRecord != null,
                appLanguage = uiState.appLanguage,
                strings = uiState.strings,
                onToggleLanguage = viewModel::onToggleAppLanguage
            )
        },
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 720.dp

            if (isWideScreen) {
                // Wide Screen: Desktop / Laptop / Tablet split-view (LEFT = Input, RIGHT = AI Output)
                Row(modifier = Modifier.fillMaxSize()) {
                    // LEFT: Code Input Panel
                    CodeInputPanel(
                        sourceCode = uiState.sourceCode,
                        selectedLanguage = uiState.selectedLanguage,
                        selectedMode = uiState.selectedMode,
                        isAnalyzing = uiState.isAnalyzing,
                        appLanguage = uiState.appLanguage,
                        onCodeChange = viewModel::onCodeChange,
                        onLanguageChange = viewModel::onLanguageChange,
                        onModeChange = viewModel::onModeChange,
                        onLoadSample = viewModel::onLoadSample,
                        onClearCode = viewModel::onClearCode,
                        onAnalyzeClick = viewModel::onAnalyzeClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    // Vertical Divider
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(DarkBorder)
                    )

                    // RIGHT: AI Output Panel
                    AnalysisOutputPanel(
                        isAnalyzing = uiState.isAnalyzing,
                        analysisProgressText = uiState.analysisProgressText,
                        streamingText = uiState.streamingText,
                        selectedLanguage = uiState.selectedLanguage,
                        selectedMode = uiState.selectedMode,
                        currentRecord = uiState.currentRecord,
                        history = uiState.history,
                        errorMessage = uiState.errorMessage,
                        isApiKeyMissing = uiState.isApiKeyMissing,
                        appLanguage = uiState.appLanguage,
                        onSelectHistoryItem = viewModel::onSelectHistoryItem,
                        onRetry = viewModel::onRetry,
                        onTrySamplePrompt = viewModel::onLoadSample,
                        onExportMarkdown = { viewModel.onExportMarkdown(context) },
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    )
                }
            } else {
                // Compact Screen (Mobile phone): Adaptive tab switcher between Editor & Analysis
                Column(modifier = Modifier.fillMaxSize()) {
                    // Mobile navigation tabs
                    MobileTabBar(
                        selectedTab = mobileSelectedTab,
                        onTabSelected = { mobileSelectedTab = it },
                        isAnalyzing = uiState.isAnalyzing,
                        hasResult = uiState.currentRecord != null,
                        strings = uiState.strings
                    )

                    Crossfade(
                        targetState = mobileSelectedTab,
                        label = "mobileTabCrossfade"
                    ) { tab ->
                        when (tab) {
                            MobileTab.EDITOR -> {
                                CodeInputPanel(
                                    sourceCode = uiState.sourceCode,
                                    selectedLanguage = uiState.selectedLanguage,
                                    selectedMode = uiState.selectedMode,
                                    isAnalyzing = uiState.isAnalyzing,
                                    appLanguage = uiState.appLanguage,
                                    onCodeChange = viewModel::onCodeChange,
                                    onLanguageChange = viewModel::onLanguageChange,
                                    onModeChange = viewModel::onModeChange,
                                    onLoadSample = viewModel::onLoadSample,
                                    onClearCode = viewModel::onClearCode,
                                    onAnalyzeClick = viewModel::onAnalyzeClick,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            MobileTab.ANALYSIS -> {
                                AnalysisOutputPanel(
                                    isAnalyzing = uiState.isAnalyzing,
                                    analysisProgressText = uiState.analysisProgressText,
                                    streamingText = uiState.streamingText,
                                    selectedLanguage = uiState.selectedLanguage,
                                    selectedMode = uiState.selectedMode,
                                    currentRecord = uiState.currentRecord,
                                    history = uiState.history,
                                    errorMessage = uiState.errorMessage,
                                    isApiKeyMissing = uiState.isApiKeyMissing,
                                    appLanguage = uiState.appLanguage,
                                    onSelectHistoryItem = viewModel::onSelectHistoryItem,
                                    onRetry = viewModel::onRetry,
                                    onTrySamplePrompt = {
                                        viewModel.onLoadSample()
                                        mobileSelectedTab = MobileTab.EDITOR
                                    },
                                    onExportMarkdown = { viewModel.onExportMarkdown(context) },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(
    isAnalyzing: Boolean,
    resultReady: Boolean,
    appLanguage: AppLanguage,
    strings: LocalizedStrings,
    onToggleLanguage: () -> Unit
) {
    Surface(
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Name & Tagline
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryBlue.copy(alpha = 0.2f))
                        .border(1.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔍", fontSize = 18.sp)
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = strings.appTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(PrimaryBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = strings.aiBadge,
                                fontFamily = JetBrainsMono,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                    }
                    Text(
                        text = strings.appTagline,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // Right header section: Subtle Korean Language Toggle + Status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Subtle Korean Language Toggle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceVariant)
                        .border(
                            1.dp,
                            if (appLanguage == AppLanguage.KOREAN) AccentGreen.copy(alpha = 0.6f) else DarkBorder,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable { onToggleLanguage() }
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                        .testTag("korean_language_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (appLanguage == AppLanguage.KOREAN) "🇰🇷 한국어" else "🇺🇸 EN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (appLanguage == AppLanguage.KOREAN) AccentGreen else TextSecondary
                        )
                    }
                }

                // Status indicator badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isAnalyzing -> AccentBlue
                                    resultReady -> AccentGreen
                                    else -> TextMuted
                                }
                            )
                    )
                    Text(
                        text = when {
                            isAnalyzing -> strings.statusAnalyzing
                            resultReady -> strings.statusReady
                            else -> strings.statusStandby
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun MobileTabBar(
    selectedTab: MobileTab,
    onTabSelected: (MobileTab) -> Unit,
    isAnalyzing: Boolean,
    hasResult: Boolean,
    strings: LocalizedStrings
) {
    Surface(
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Editor Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == MobileTab.EDITOR) DarkSurfaceVariant else Color.Transparent)
                    .border(
                        1.dp,
                        if (selectedTab == MobileTab.EDITOR) PrimaryBlue else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onTabSelected(MobileTab.EDITOR) }
                    .padding(vertical = 10.dp)
                    .testTag("tab_editor"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = null,
                        tint = if (selectedTab == MobileTab.EDITOR) PrimaryBlue else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = strings.tabEditor,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == MobileTab.EDITOR) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == MobileTab.EDITOR) TextPrimary else TextSecondary
                    )
                }
            }

            // Analysis Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selectedTab == MobileTab.ANALYSIS) DarkSurfaceVariant else Color.Transparent)
                    .border(
                        1.dp,
                        if (selectedTab == MobileTab.ANALYSIS) PrimaryBlue else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    )
                    .clickable { onTabSelected(MobileTab.ANALYSIS) }
                    .padding(vertical = 10.dp)
                    .testTag("tab_analysis"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Insights,
                        contentDescription = null,
                        tint = if (selectedTab == MobileTab.ANALYSIS) AccentGreen else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isAnalyzing) strings.statusAnalyzing else strings.tabOutput,
                        fontSize = 13.sp,
                        fontWeight = if (selectedTab == MobileTab.ANALYSIS) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedTab == MobileTab.ANALYSIS) TextPrimary else TextSecondary
                    )
                    if (hasResult) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AccentGreen)
                        )
                    }
                }
            }
        }
    }
}
