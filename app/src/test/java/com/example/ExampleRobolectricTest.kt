package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AnalysisMode
import com.example.data.model.AnalysisRecord
import com.example.data.model.AppLanguage
import com.example.data.model.CodeLanguage
import com.example.data.model.DifficultyLevel
import com.example.data.model.ExplainResult
import com.example.data.model.WalkthroughStep
import com.example.ui.CodeLensViewModel
import com.example.util.MarkdownExporter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("CodeLens", appName)
    }

    @Test
    fun `test viewmodel sample loading and clearing`() {
        val viewModel = CodeLensViewModel()
        assertTrue(viewModel.uiState.value.isCodeEmpty)

        // Load sample
        viewModel.onLoadSample()
        assertFalse(viewModel.uiState.value.isCodeEmpty)
        assertTrue(viewModel.uiState.value.sourceCode.contains("find_average_and_max"))

        // Change language
        viewModel.onLanguageChange(CodeLanguage.JAVASCRIPT)
        viewModel.onLoadSample()
        assertTrue(viewModel.uiState.value.sourceCode.contains("fetchUserData"))

        // Clear code
        viewModel.onClearCode()
        assertTrue(viewModel.uiState.value.isCodeEmpty)

        // Mode change
        viewModel.onModeChange(AnalysisMode.DEBUG)
        assertEquals(AnalysisMode.DEBUG, viewModel.uiState.value.selectedMode)
    }

    @Test
    fun `test language toggle switches UI strings`() {
        val viewModel = CodeLensViewModel()
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.appLanguage)
        assertEquals("Analyze Code", viewModel.uiState.value.strings.analyzeCode)

        viewModel.onToggleAppLanguage()
        assertEquals(AppLanguage.KOREAN, viewModel.uiState.value.appLanguage)
        assertEquals("코드 분석하기", viewModel.uiState.value.strings.analyzeCode)
        assertEquals("난이도 평가", viewModel.uiState.value.strings.difficultyTitle)

        viewModel.onToggleAppLanguage()
        assertEquals(AppLanguage.ENGLISH, viewModel.uiState.value.appLanguage)
        assertEquals("Analyze Code", viewModel.uiState.value.strings.analyzeCode)
    }

    @Test
    fun `test difficulty rating and markdown export formatting`() {
        val record = AnalysisRecord(
            language = CodeLanguage.PYTHON,
            mode = AnalysisMode.EXPLAIN,
            sourceCode = "def add(a, b):\n    return a + b",
            explainResult = ExplainResult(
                overview = "Simple addition function",
                difficulty = DifficultyLevel.BEGINNER,
                walkthrough = listOf(WalkthroughStep("Line 1-2", "Defines addition and returns sum")),
                keyConcepts = listOf("Function", "Arithmetic"),
                timeComplexity = "O(1)",
                timeJustification = "Constant operation",
                spaceComplexity = "O(1)",
                spaceJustification = "No heap allocation",
                learningResources = emptyList()
            )
        )

        assertEquals("Beginner", DifficultyLevel.BEGINNER.label(AppLanguage.ENGLISH))
        assertEquals("초급", DifficultyLevel.BEGINNER.label(AppLanguage.KOREAN))

        val md = MarkdownExporter.generateMarkdown(record, AppLanguage.ENGLISH)
        assertTrue(md.contains("# CodeLens Analysis Report"))
        assertTrue(md.contains("- **Difficulty Rating**: 🟢 Beginner"))
        assertTrue(md.contains("## 1. Overview"))
        assertTrue(md.contains("Simple addition function"))

        val mdKo = MarkdownExporter.generateMarkdown(record, AppLanguage.KOREAN)
        assertTrue(mdKo.contains("- **Difficulty Rating**: 🟢 초급"))
    }
}
