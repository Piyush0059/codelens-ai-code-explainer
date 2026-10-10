package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.gemini.GeminiAnalysisResponse
import com.example.data.gemini.GeminiService
import com.example.data.model.AnalysisMode
import com.example.data.model.AnalysisRecord
import com.example.data.model.CodeLanguage
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.example.data.model.AppLanguage
import com.example.data.model.Translations
import com.example.util.MarkdownExporter

data class CodeLensUiState(
    val sourceCode: String = "",
    val selectedLanguage: CodeLanguage = CodeLanguage.PYTHON,
    val selectedMode: AnalysisMode = AnalysisMode.EXPLAIN,
    val appLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isAnalyzing: Boolean = false,
    val analysisProgressText: String = "Analyzing your code...",
    val streamingText: String = "",
    val currentRecord: AnalysisRecord? = null,
    val history: List<AnalysisRecord> = emptyList(),
    val errorMessage: String? = null,
    val isApiKeyMissing: Boolean = false
) {
    val strings: com.example.data.model.LocalizedStrings
        get() = Translations.get(appLanguage)

    val isCodeEmpty: Boolean
        get() = sourceCode.trim().isEmpty()

    val charCount: Int
        get() = sourceCode.length

    val lineCount: Int
        get() = if (sourceCode.isEmpty()) 0 else sourceCode.lines().size
}

class CodeLensViewModel(
    private val geminiService: GeminiService = GeminiService()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CodeLensUiState())
    val uiState: StateFlow<CodeLensUiState> = _uiState.asStateFlow()

    private var analysisJob: Job? = null

    fun onToggleAppLanguage() {
        _uiState.update { current ->
            val next = if (current.appLanguage == AppLanguage.ENGLISH) AppLanguage.KOREAN else AppLanguage.ENGLISH
            current.copy(appLanguage = next)
        }
    }

    fun onExportMarkdown(context: android.content.Context) {
        val record = _uiState.value.currentRecord ?: return
        MarkdownExporter.exportAndShareMarkdown(context, record, _uiState.value.appLanguage)
    }

    fun onCodeChange(newCode: String) {
        _uiState.update { it.copy(sourceCode = newCode, errorMessage = null) }
    }

    fun onLanguageChange(language: CodeLanguage) {
        _uiState.update { it.copy(selectedLanguage = language) }
    }

    fun onModeChange(mode: AnalysisMode) {
        _uiState.update { it.copy(selectedMode = mode) }
    }

    fun onLoadSample() {
        val currentLang = _uiState.value.selectedLanguage
        _uiState.update {
            it.copy(
                sourceCode = currentLang.sampleCode,
                errorMessage = null
            )
        }
    }

    fun onClearCode() {
        _uiState.update {
            it.copy(
                sourceCode = "",
                errorMessage = null
            )
        }
    }

    fun onSelectHistoryItem(record: AnalysisRecord) {
        _uiState.update {
            it.copy(
                currentRecord = record,
                streamingText = "",
                sourceCode = record.sourceCode,
                selectedLanguage = record.language,
                selectedMode = record.mode,
                errorMessage = null
            )
        }
    }

    fun onAnalyzeClick() {
        val currentState = _uiState.value
        val codeToAnalyze = currentState.sourceCode.trim()
        if (codeToAnalyze.isEmpty()) return

        analysisJob?.cancel()
        analysisJob = viewModelScope.launch {
            // 1. Immediately show progress spinner
            _uiState.update {
                it.copy(
                    isAnalyzing = true,
                    streamingText = "",
                    currentRecord = null,
                    errorMessage = null,
                    isApiKeyMissing = false,
                    analysisProgressText = "Analyzing your code..."
                )
            }

            try {
                // 2. Stream chunks word-by-word into UI
                val response = geminiService.analyzeCodeStream(
                    sourceCode = codeToAnalyze,
                    language = currentState.selectedLanguage,
                    mode = currentState.selectedMode,
                    onChunk = { chunk ->
                        _uiState.update { current ->
                            current.copy(
                                streamingText = current.streamingText + chunk
                            )
                        }
                    }
                )

                when (response) {
                    is GeminiAnalysisResponse.SuccessExplain -> {
                        val record = AnalysisRecord(
                            language = currentState.selectedLanguage,
                            mode = currentState.selectedMode,
                            sourceCode = codeToAnalyze,
                            explainResult = response.result,
                            rawMarkdown = response.markdown
                        )
                        addRecordToHistory(record)
                    }

                    is GeminiAnalysisResponse.SuccessDebug -> {
                        val record = AnalysisRecord(
                            language = currentState.selectedLanguage,
                            mode = currentState.selectedMode,
                            sourceCode = codeToAnalyze,
                            debugResult = response.result,
                            rawMarkdown = response.markdown
                        )
                        addRecordToHistory(record)
                    }

                    is GeminiAnalysisResponse.SuccessOptimize -> {
                        val record = AnalysisRecord(
                            language = currentState.selectedLanguage,
                            mode = currentState.selectedMode,
                            sourceCode = codeToAnalyze,
                            optimizeResult = response.result,
                            rawMarkdown = response.markdown
                        )
                        addRecordToHistory(record)
                    }

                    is GeminiAnalysisResponse.Error -> {
                        _uiState.update {
                            it.copy(
                                isAnalyzing = false,
                                streamingText = "",
                                errorMessage = response.message,
                                isApiKeyMissing = response.isApiKeyMissing
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isAnalyzing = false,
                        streamingText = "",
                        errorMessage = e.localizedMessage ?: "Something went wrong, please try again",
                        isApiKeyMissing = false
                    )
                }
            }
        }
    }

    private fun addRecordToHistory(record: AnalysisRecord) {
        _uiState.update { current ->
            // Keep last 3 analyses in session as required!
            val updatedHistory = (listOf(record) + current.history.filter { it.id != record.id }).take(3)
            current.copy(
                isAnalyzing = false,
                streamingText = "",
                currentRecord = record,
                history = updatedHistory,
                errorMessage = null
            )
        }
    }

    fun onRetry() {
        onAnalyzeClick()
    }
}
