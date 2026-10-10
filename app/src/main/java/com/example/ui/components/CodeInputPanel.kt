package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalysisMode
import com.example.data.model.CodeLanguage
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentYellow
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

import com.example.data.model.AppLanguage
import com.example.data.model.Translations

@Composable
fun CodeInputPanel(
    sourceCode: String,
    selectedLanguage: CodeLanguage,
    selectedMode: AnalysisMode,
    isAnalyzing: Boolean,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onCodeChange: (String) -> Unit,
    onLanguageChange: (CodeLanguage) -> Unit,
    onModeChange: (AnalysisMode) -> Unit,
    onLoadSample: () -> Unit,
    onClearCode: () -> Unit,
    onAnalyzeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = remember(appLanguage) { Translations.get(appLanguage) }
    var languageDropdownExpanded by remember { mutableStateOf(false) }
    val isCodeEmpty = sourceCode.trim().isEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Language selector row & "Try a sample" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Language dropdown
            Box {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DarkSurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .clickable { languageDropdownExpanded = true }
                        .testTag("language_selector")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = selectedLanguage.iconEmoji,
                            fontSize = 16.sp
                        )
                        Text(
                            text = selectedLanguage.displayName,
                            color = TextPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select language",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = languageDropdownExpanded,
                    onDismissRequest = { languageDropdownExpanded = false },
                    modifier = Modifier
                        .background(DarkSurface)
                        .border(1.dp, DarkBorder, RoundedCornerShape(8.dp))
                ) {
                    CodeLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = lang.iconEmoji, fontSize = 16.sp)
                                    Text(
                                        text = lang.displayName,
                                        color = if (lang == selectedLanguage) AccentGreen else TextPrimary,
                                        fontWeight = if (lang == selectedLanguage) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            },
                            onClick = {
                                onLanguageChange(lang)
                                languageDropdownExpanded = false
                            },
                            modifier = Modifier.testTag("lang_option_${lang.id}")
                        )
                    }
                }
            }

            // "Try a sample" button
            OutlinedButton(
                onClick = onLoadSample,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkSurfaceVariant.copy(alpha = 0.5f),
                    contentColor = TextPrimary
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("try_sample_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = AccentYellow,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = strings.trySample,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. Mode tabs (three tabs, only one active at a time)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AnalysisMode.entries.forEach { mode ->
                    val isActive = mode == selectedMode
                    val modeLabel = when (mode) {
                        AnalysisMode.EXPLAIN -> strings.modeExplain
                        AnalysisMode.DEBUG -> strings.modeDebug
                        AnalysisMode.OPTIMIZE -> strings.modeOptimize
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isActive) DarkSurfaceVariant else Color.Transparent)
                            .border(
                                width = if (isActive) 1.dp else 0.dp,
                                color = if (isActive) PrimaryBlue else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onModeChange(mode) }
                            .padding(vertical = 10.dp)
                            .testTag("mode_tab_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = mode.iconEmoji,
                                fontSize = 14.sp
                            )
                            Text(
                                text = modeLabel,
                                color = if (isActive) TextPrimary else TextSecondary,
                                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Large code editor textarea with line numbers feel
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Editor header bar: status & clear
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Code,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "code.${selectedLanguage.fileExtension}",
                            fontFamily = JetBrainsMono,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }

                    // Quick clear icon button if there is code
                    if (sourceCode.isNotEmpty()) {
                        IconButton(
                            onClick = onClearCode,
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("quick_clear_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear code",
                                tint = TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Code input area with line numbers
                val lineCount = remember(sourceCode) {
                    if (sourceCode.isEmpty()) 1 else sourceCode.lines().size
                }
                val scrollState = rememberScrollState()

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 8.dp)
                        .verticalScroll(scrollState)
                ) {
                    // Line numbers column
                    Column(
                        modifier = Modifier
                            .width(42.dp)
                            .padding(top = 12.dp, end = 8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        for (i in 1..maxOf(lineCount, 12)) {
                            Text(
                                text = "$i",
                                fontFamily = JetBrainsMono,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = if (i <= lineCount && sourceCode.isNotEmpty()) TextMuted else TextMuted.copy(alpha = 0.4f)
                            )
                        }
                    }

                    // Divider between gutter and code
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .fillMaxHeight()
                            .background(DarkBorderSubtle)
                    )

                    // Code text field
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        if (sourceCode.isEmpty()) {
                            Text(
                                text = strings.codePlaceholder,
                                fontFamily = JetBrainsMono,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = TextMuted
                            )
                        }

                        BasicTextField(
                            value = sourceCode,
                            onValueChange = onCodeChange,
                            textStyle = TextStyle(
                                fontFamily = JetBrainsMono,
                                fontSize = 13.sp,
                                lineHeight = 20.sp,
                                color = TextPrimary
                            ),
                            cursorBrush = SolidColor(PrimaryBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("code_editor_input")
                        )
                    }
                }

                // Editor footer: character counter & line counter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurfaceVariant.copy(alpha = 0.4f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isCodeEmpty) strings.editorEmptyHint.take(20) else "${selectedLanguage.displayName} • UTF-8",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "${sourceCode.length} ${strings.charsLabel} • $lineCount ${strings.linesLabel}",
                        fontFamily = JetBrainsMono,
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.testTag("char_counter")
                    )
                }
            }
        }

        // 4. Inline hint if empty
        if (isCodeEmpty) {
            Text(
                text = strings.editorEmptyHint,
                fontSize = 12.sp,
                color = AccentYellow,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        // 5. Action Buttons: "Clear" and "Analyze Code"
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onClearCode,
                enabled = !isCodeEmpty && !isAnalyzing,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = TextSecondary,
                    disabledContentColor = TextMuted
                ),
                modifier = Modifier
                    .weight(0.35f)
                    .height(48.dp)
                    .testTag("clear_button")
            ) {
                Text(
                    text = strings.clear,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onAnalyzeClick,
                enabled = !isCodeEmpty && !isAnalyzing,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkGreen,
                    contentColor = Color.White,
                    disabledContainerColor = DarkSurfaceVariant,
                    disabledContentColor = TextMuted
                ),
                modifier = Modifier
                    .weight(0.65f)
                    .height(48.dp)
                    .testTag("analyze_code_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAnalyzing) strings.analyzingButton else strings.analyzeCode,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
