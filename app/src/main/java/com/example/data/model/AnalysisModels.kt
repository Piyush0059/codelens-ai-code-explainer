package com.example.data.model

import java.util.UUID

// Difficulty Rating for Explain mode
enum class DifficultyLevel(
    val labelEn: String,
    val labelKo: String,
    val emoji: String,
    val badgeColorHex: Long
) {
    BEGINNER("Beginner", "초급", "🟢", 0xFF238636),
    INTERMEDIATE("Intermediate", "중급", "🟡", 0xFFD29922),
    ADVANCED("Advanced", "고급", "🔴", 0xFFDA3633);

    fun label(lang: AppLanguage): String = if (lang == AppLanguage.KOREAN) labelKo else labelEn

    companion object {
        fun fromString(str: String?): DifficultyLevel {
            if (str == null) return INTERMEDIATE
            val upper = str.uppercase()
            return when {
                upper.contains("ADV") || upper.contains("고급") || upper.contains("HARD") -> ADVANCED
                upper.contains("BEG") || upper.contains("초급") || upper.contains("EASY") -> BEGINNER
                else -> INTERMEDIATE
            }
        }
    }
}

// Explain Mode Models
data class WalkthroughStep(
    val lineRange: String,
    val explanation: String
)

data class LearningResource(
    val topic: String,
    val description: String
)

data class ExplainResult(
    val overview: String,
    val difficulty: DifficultyLevel = DifficultyLevel.INTERMEDIATE,
    val walkthrough: List<WalkthroughStep>,
    val keyConcepts: List<String>,
    val timeComplexity: String,
    val timeJustification: String,
    val spaceComplexity: String,
    val spaceJustification: String,
    val learningResources: List<LearningResource>
)

// Debug Mode Models
enum class BugSeverity(val label: String, val icon: String) {
    CRITICAL("Critical", "🔴"),
    WARNING("Warning", "🟡")
}

data class BugItem(
    val severity: BugSeverity,
    val lineNumber: String,
    val whatIsWrong: String,
    val whyItFails: String
)

data class DebugResult(
    val bugs: List<BugItem>,
    val fixedCode: String,
    val edgeCases: List<String>,
    val bestPractices: List<String>
)

// Optimize Mode Models
data class ComparisonRow(
    val metric: String,
    val before: String,
    val after: String
)

data class AlternativeApproach(
    val title: String,
    val explanation: String,
    val codeSnippet: String
)

data class OptimizeResult(
    val bottlenecks: String,
    val currentComplexity: String,
    val optimizedCode: String,
    val comparisonTable: List<ComparisonRow>,
    val alternativeApproach: AlternativeApproach?
)

// Overall Analysis Session Record (for history)
data class AnalysisRecord(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val language: CodeLanguage,
    val mode: AnalysisMode,
    val sourceCode: String,
    val explainResult: ExplainResult? = null,
    val debugResult: DebugResult? = null,
    val optimizeResult: OptimizeResult? = null,
    val rawMarkdown: String = ""
)
