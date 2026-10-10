package com.example.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.AnalysisMode
import com.example.data.model.AnalysisRecord
import com.example.data.model.AppLanguage
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MarkdownExporter {

    fun generateMarkdown(record: AnalysisRecord, appLanguage: AppLanguage): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateString = dateFormat.format(Date(record.timestamp))

        return buildString {
            appendLine("# CodeLens Analysis Report")
            appendLine()
            appendLine("- **Timestamp**: $dateString")
            appendLine("- **Language**: ${record.language.displayName}")
            appendLine("- **Analysis Mode**: ${record.mode.title}")

            if (record.mode == AnalysisMode.EXPLAIN && record.explainResult != null) {
                val diff = record.explainResult.difficulty
                appendLine("- **Difficulty Rating**: ${diff.emoji} ${diff.label(appLanguage)}")
            }

            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Analyzed Source Code")
            appendLine("```${record.language.fileExtension}")
            appendLine(record.sourceCode.trim())
            appendLine("```")
            appendLine()

            when (record.mode) {
                AnalysisMode.EXPLAIN -> {
                    record.explainResult?.let { res ->
                        appendLine("## 1. Overview")
                        appendLine(res.overview)
                        appendLine()
                        appendLine("- **Difficulty Level**: ${res.difficulty.emoji} ${res.difficulty.label(appLanguage)}")
                        appendLine()

                        appendLine("## 2. Line-by-Line Walkthrough")
                        res.walkthrough.forEachIndexed { idx, step ->
                            appendLine("### ${step.lineRange}")
                            appendLine(step.explanation)
                            appendLine()
                        }

                        appendLine("## 3. Key Concepts")
                        res.keyConcepts.forEach { concept ->
                            appendLine("- **$concept**")
                        }
                        appendLine()

                        appendLine("## 4. Complexity Analysis")
                        appendLine("- **Time Complexity**: `${res.timeComplexity}` — ${res.timeJustification}")
                        appendLine("- **Space Complexity**: `${res.spaceComplexity}` — ${res.spaceJustification}")
                        appendLine()

                        appendLine("## 5. Learning Resources")
                        res.learningResources.forEach { resource ->
                            appendLine("- **${resource.topic}**: ${resource.description}")
                        }
                        appendLine()
                    }
                }

                AnalysisMode.DEBUG -> {
                    record.debugResult?.let { res ->
                        appendLine("## 1. Bugs Found (${res.bugs.size})")
                        res.bugs.forEachIndexed { idx, bug ->
                            appendLine("### Bug #${idx + 1} [${bug.severity.icon} ${bug.severity.label}] - Line ${bug.lineNumber}")
                            appendLine("- **What is wrong**: ${bug.whatIsWrong}")
                            appendLine("- **Why it fails**: ${bug.whyItFails}")
                            appendLine()
                        }

                        appendLine("## 2. Fixed Code")
                        appendLine("```${record.language.fileExtension}")
                        appendLine(res.fixedCode.trim())
                        appendLine("```")
                        appendLine()

                        appendLine("## 3. Edge Cases")
                        res.edgeCases.forEach { edge ->
                            appendLine("- $edge")
                        }
                        appendLine()

                        appendLine("## 4. Best Practice Notes")
                        res.bestPractices.forEach { practice ->
                            appendLine("- $practice")
                        }
                        appendLine()
                    }
                }

                AnalysisMode.OPTIMIZE -> {
                    record.optimizeResult?.let { res ->
                        appendLine("## 1. Bottlenecks")
                        appendLine(res.bottlenecks)
                        appendLine()
                        appendLine("- **Current Complexity**: `${res.currentComplexity}`")
                        appendLine()

                        appendLine("## 2. Optimized Code")
                        appendLine("```${record.language.fileExtension}")
                        appendLine(res.optimizedCode.trim())
                        appendLine("```")
                        appendLine()

                        appendLine("## 3. Before vs After Comparison")
                        appendLine("| Metric | Before | After |")
                        appendLine("| :--- | :--- | :--- |")
                        res.comparisonTable.forEach { row ->
                            appendLine("| ${row.metric} | `${row.before}` | `${row.after}` |")
                        }
                        appendLine()

                        res.alternativeApproach?.let { alt ->
                            appendLine("## 4. Alternative Approach: ${alt.title}")
                            appendLine(alt.explanation)
                            appendLine()
                            if (alt.codeSnippet.isNotBlank()) {
                                appendLine("```${record.language.fileExtension}")
                                appendLine(alt.codeSnippet.trim())
                                appendLine("```")
                                appendLine()
                            }
                        }
                    }
                }
            }

            appendLine("---")
            appendLine("*Generated by [CodeLens](https://github.com) — AI Code Explainer & Debugger*")
        }
    }

    fun exportAndShareMarkdown(context: Context, record: AnalysisRecord, appLanguage: AppLanguage) {
        try {
            val content = generateMarkdown(record, appLanguage)
            val fileName = "codelens_${record.language.id}_${record.mode.name.lowercase()}_${System.currentTimeMillis()}.md"

            // Save to app cache directory
            val cacheFile = File(context.cacheDir, fileName)
            cacheFile.writeText(content, Charsets.UTF_8)

            // Share intent so the user can save to downloads, Google Drive, Docs, or email
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_SUBJECT, "CodeLens Analysis - ${record.language.displayName} (${record.mode.title})")
                putExtra(Intent.EXTRA_TEXT, content)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            val chooser = Intent.createChooser(sendIntent, "Export CodeLens Markdown")
            chooser.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
