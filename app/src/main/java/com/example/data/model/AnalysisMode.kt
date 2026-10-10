package com.example.data.model

enum class AnalysisMode(
    val title: String,
    val iconEmoji: String,
    val description: String
) {
    EXPLAIN(
        title = "Explain",
        iconEmoji = "📖",
        description = "Line-by-line walkthrough, concepts, complexity & learning resources"
    ),
    DEBUG(
        title = "Debug & Fix",
        iconEmoji = "🐞",
        description = "Detect bugs, severity, edge cases & provide verified fixed code"
    ),
    OPTIMIZE(
        title = "Optimize",
        iconEmoji = "⚡",
        description = "Bottleneck analysis, before vs after metrics & alternative approach"
    );

    val fullLabel: String
        get() = "$iconEmoji $title"
}
