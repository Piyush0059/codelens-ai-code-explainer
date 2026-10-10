package com.example.data.gemini

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AlternativeApproach
import com.example.data.model.AnalysisMode
import com.example.data.model.BugItem
import com.example.data.model.BugSeverity
import com.example.data.model.CodeLanguage
import com.example.data.model.ComparisonRow
import com.example.data.model.DebugResult
import com.example.data.model.DifficultyLevel
import com.example.data.model.ExplainResult
import com.example.data.model.LearningResource
import com.example.data.model.OptimizeResult
import com.example.data.model.WalkthroughStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

sealed class GeminiAnalysisResponse {
    data class SuccessExplain(val result: ExplainResult, val markdown: String) : GeminiAnalysisResponse()
    data class SuccessDebug(val result: DebugResult, val markdown: String) : GeminiAnalysisResponse()
    data class SuccessOptimize(val result: OptimizeResult, val markdown: String) : GeminiAnalysisResponse()
    data class Error(val message: String, val isApiKeyMissing: Boolean = false) : GeminiAnalysisResponse()
}

class GeminiService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Fastest flash-lite model per Gemini API skill with ultra-fast fallback chain
    private val candidateModels = listOf(
        "gemini-3.1-flash-lite-preview", // Fastest flash-lite model
        "gemini-2.0-flash",              // Requested fastest flash model
        "gemini-3.8-flash"               // High-speed fallback
    )

    suspend fun analyzeCodeStream(
        sourceCode: String,
        language: CodeLanguage,
        mode: AnalysisMode,
        onChunk: suspend (String) -> Unit
    ): GeminiAnalysisResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext GeminiAnalysisResponse.Error(
                message = "API key is not configured. Please set GEMINI_API_KEY in the Secrets panel in AI Studio.",
                isApiKeyMissing = true
            )
        }

        val systemPrompt = buildSystemPrompt(mode, language)
        val userPrompt = buildUserPrompt(mode, language, sourceCode)

        try {
            val fullText = streamWithModelCandidates(apiKey, systemPrompt, userPrompt, onChunk)
            parseMarkdownResult(fullText, mode, language)
        } catch (e: Exception) {
            Log.e("GeminiService", "Analysis stream failed", e)
            GeminiAnalysisResponse.Error(
                message = e.localizedMessage ?: "Failed to connect to AI analysis service. Please check your network and try again."
            )
        }
    }

    suspend fun analyzeCode(
        sourceCode: String,
        language: CodeLanguage,
        mode: AnalysisMode
    ): GeminiAnalysisResponse = analyzeCodeStream(sourceCode, language, mode) { /* no-op */ }

    private suspend fun streamWithModelCandidates(
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        onChunk: suspend (String) -> Unit
    ): String {
        var lastException: Exception? = null

        for (model in candidateModels) {
            try {
                return executeStreamingRequest(model, apiKey, systemPrompt, userPrompt, onChunk)
            } catch (e: Exception) {
                Log.w("GeminiService", "Model $model failed: ${e.message}, trying next available model", e)
                lastException = e
            }
        }
        throw lastException ?: IOException("Failed to stream response from any available AI model")
    }

    private suspend fun executeStreamingRequest(
        model: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        onChunk: suspend (String) -> Unit
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?alt=sse&key=$apiKey"

        val requestJson = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply { put("text", userPrompt) })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", systemPrompt) })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.1) // Low temperature for fastest, crispest output
                put("maxOutputTokens", 2048)
            })
        }

        val body = requestJson.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        val response = okHttpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            val errorBody = response.body?.string().orEmpty()
            Log.e("GeminiService", "HTTP ${response.code} on model $model: $errorBody")
            throw IOException("Model $model returned HTTP ${response.code}")
        }

        val responseBody = response.body ?: throw IOException("Empty response stream")
        val accumulatedText = StringBuilder()

        BufferedReader(InputStreamReader(responseBody.byteStream(), Charsets.UTF_8)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val rawLine = line?.trim().orEmpty()
                if (!rawLine.startsWith("data:")) continue
                val payload = rawLine.removePrefix("data:").trim()
                if (payload.isEmpty() || payload == "[DONE]") continue

                try {
                    val chunkJson = JSONObject(payload)
                    val candidates = chunkJson.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val textPiece = parts.getJSONObject(0).optString("text")
                            if (textPiece.isNotEmpty()) {
                                accumulatedText.append(textPiece)
                                onChunk(textPiece)
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Ignore non-fatal chunk formatting anomalies
                }
            }
        }

        val resultString = accumulatedText.toString().trim()
        if (resultString.isBlank()) {
            throw IOException("No content streamed by model $model")
        }
        return resultString
    }

    private fun buildSystemPrompt(mode: AnalysisMode, language: CodeLanguage): String {
        val concisenessMandate = """
STRICT CONCISENESS & SPEED RULES:
- Skip long explanations, intros, and conversational fluff. ONLY show key points.
- Every explanation must be 1 to 2 punchy sentences maximum.
- Format cleanly in structured Markdown with exact standard headings.
- Language: ${language.displayName}.
""".trimIndent()

        return when (mode) {
            AnalysisMode.EXPLAIN -> """
You are CodeLens, a fast AI programming explainer.
$concisenessMandate

Output MUST strictly follow this Markdown structure:

## 1. Overview
[Difficulty: BEGINNER] (or INTERMEDIATE or ADVANCED based on code complexity)
(1-2 concise sentences summarizing what the code does)

## 2. Line-by-Line Walkthrough
- **Lines X-Y**: (1-sentence concise explanation of this step)
- **Lines A-B**: (1-sentence concise explanation of this step)
(Limit to 2 to 4 key milestones only)

## 3. Key Concepts
- Concept 1: Short note
- Concept 2: Short note
(2 to 4 key concepts/data structures only)

## 4. Complexity Analysis
- **Time Complexity**: `O(...)` — 1-sentence justification.
- **Space Complexity**: `O(...)` — 1-sentence justification.

## 5. Learning Resources
- **Topic**: 1-sentence study recommendation.
""".trimIndent()

            AnalysisMode.DEBUG -> """
You are CodeLens, a fast AI debugger and security auditor.
$concisenessMandate

Output MUST strictly follow this Markdown structure:

## 1. Bugs Found
1. 🔴 **CRITICAL** (Line X): Short description of bug. Why it fails.
2. 🟡 **WARNING** (Line Y): Short description of warning. Why it fails.
(If no bugs exist, output: "No critical bugs identified in this snippet.")

## 2. Fixed Code
```${language.fileExtension}
(Complete corrected ${language.displayName} code in a single copyable block ready to run, without unnecessary comments)
```

## 3. Edge Cases
- Edge case 1 that breaks original code.
- Edge case 2 that breaks original code.

## 4. Best Practice Notes
- Key convention or guideline note.
""".trimIndent()

            AnalysisMode.OPTIMIZE -> """
You are CodeLens, a fast algorithm and performance optimization architect.
$concisenessMandate

Output MUST strictly follow this Markdown structure:

## 1. Bottlenecks
(1-2 punchy sentences explaining the bottleneck) Current Complexity: `O(...)`.

## 2. Optimized Code
```${language.fileExtension}
(Complete optimized ${language.displayName} implementation in a single copyable block)
```

## 3. Before vs After Comparison
| Metric | Before | After |
| Time Complexity | O(...) | O(...) |
| Space Complexity | O(...) | O(...) |
| Readability Score | X/10 | Y/10 |

## 4. Alternative Approach
**Title**: 1-sentence summary of alternative.
```${language.fileExtension}
(Short code snippet)
```
""".trimIndent()
        }
    }

    private fun buildUserPrompt(mode: AnalysisMode, language: CodeLanguage, sourceCode: String): String {
        return """
Analyze this ${language.displayName} code in ${mode.title} mode:

```${language.fileExtension}
$sourceCode
```

Remember: Be ultra-concise, skip long explanations, and only show key points following the markdown sections.
""".trimIndent()
    }

    private fun parseMarkdownResult(
        markdown: String,
        mode: AnalysisMode,
        language: CodeLanguage
    ): GeminiAnalysisResponse {
        return when (mode) {
            AnalysisMode.EXPLAIN -> {
                val result = parseExplainMarkdown(markdown, language)
                GeminiAnalysisResponse.SuccessExplain(result, markdown)
            }
            AnalysisMode.DEBUG -> {
                val result = parseDebugMarkdown(markdown, language)
                GeminiAnalysisResponse.SuccessDebug(result, markdown)
            }
            AnalysisMode.OPTIMIZE -> {
                val result = parseOptimizeMarkdown(markdown, language)
                GeminiAnalysisResponse.SuccessOptimize(result, markdown)
            }
        }
    }

    private fun splitMarkdownSections(markdown: String): Map<String, String> {
        val sections = mutableMapOf<String, StringBuilder>()
        var currentHeader = "Intro"

        for (line in markdown.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("## ")) {
                currentHeader = trimmed.removePrefix("## ").trim()
                sections[currentHeader] = StringBuilder()
            } else if (trimmed.startsWith("### ") && currentHeader == "Intro") {
                currentHeader = trimmed.removePrefix("### ").trim()
                sections[currentHeader] = StringBuilder()
            } else {
                sections.getOrPut(currentHeader) { StringBuilder() }.appendLine(line)
            }
        }
        return sections.mapValues { it.value.toString().trim() }
    }

    private fun parseExplainMarkdown(markdown: String, language: CodeLanguage): ExplainResult {
        val sections = splitMarkdownSections(markdown)

        // Find section containing "Overview"
        val overviewEntry = sections.entries.firstOrNull { it.key.contains("Overview", ignoreCase = true) }
        val overviewRaw = overviewEntry?.value ?: markdown.take(300)

        // Extract difficulty level
        val diffMatch = Regex("\\[Difficulty:\\s*([A-Za-z가-힣]+)\\]", RegexOption.IGNORE_CASE).find(overviewRaw)
            ?: Regex("(Beginner|Intermediate|Advanced|초급|중급|고급)", RegexOption.IGNORE_CASE).find(overviewRaw)
        val diffLevel = DifficultyLevel.fromString(diffMatch?.groupValues?.get(1))

        val cleanOverview = overviewRaw
            .replace(Regex("\\[Difficulty:[^\\]]+\\]"), "")
            .trim()
            .ifEmpty { "Concise summary of ${language.displayName} code." }

        // Walkthrough
        val wtEntry = sections.entries.firstOrNull { it.key.contains("Walkthrough", ignoreCase = true) }
        val wtRaw = wtEntry?.value.orEmpty()
        val walkthrough = mutableListOf<WalkthroughStep>()
        for (line in wtRaw.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                val clean = trimmed.removePrefix("-").removePrefix("*").removePrefix("•").trim()
                val colonIdx = clean.indexOf(':')
                if (colonIdx != -1) {
                    val range = clean.substring(0, colonIdx).replace("**", "").replace("`", "").trim()
                    val exp = clean.substring(colonIdx + 1).trim()
                    walkthrough.add(WalkthroughStep(range.ifEmpty { "Step" }, exp))
                } else {
                    walkthrough.add(WalkthroughStep("Milestone", clean))
                }
            }
        }
        if (walkthrough.isEmpty()) {
            walkthrough.add(WalkthroughStep("Flow", wtRaw.ifEmpty { "Core logical block execution." }))
        }

        // Key Concepts
        val cEntry = sections.entries.firstOrNull { it.key.contains("Concept", ignoreCase = true) }
        val cRaw = cEntry?.value.orEmpty()
        val concepts = mutableListOf<String>()
        for (line in cRaw.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                val clean = trimmed.removePrefix("-").removePrefix("*").removePrefix("•")
                    .replace("**", "").replace("`", "").trim()
                if (clean.isNotEmpty()) {
                    val title = clean.split(":").first().trim()
                    concepts.add(title)
                }
            }
        }
        if (concepts.isEmpty()) {
            concepts.addAll(listOf(language.displayName, "Algorithm", "Control Flow"))
        }

        // Complexity Analysis
        val compEntry = sections.entries.firstOrNull { it.key.contains("Complexity", ignoreCase = true) }
        val compRaw = compEntry?.value.orEmpty()
        var timeComp = "O(N)"
        var timeJust = "Linear scan."
        var spaceComp = "O(1)"
        var spaceJust = "Constant memory."

        val timeRegex = Regex("Time Complexity\\*?\\*?:?\\s*[`$]?([A-Za-z0-9^()O+*\\s]+)[`$]?\\s*[-—:]?\\s*(.*)", RegexOption.IGNORE_CASE)
        val spaceRegex = Regex("Space Complexity\\*?\\*?:?\\s*[`$]?([A-Za-z0-9^()O+*\\s]+)[`$]?\\s*[-—:]?\\s*(.*)", RegexOption.IGNORE_CASE)

        for (line in compRaw.lines()) {
            val tm = timeRegex.find(line)
            if (tm != null) {
                timeComp = tm.groupValues[1].replace("$", "").trim()
                val just = tm.groupValues[2].trim()
                if (just.isNotEmpty()) timeJust = just
            }
            val sm = spaceRegex.find(line)
            if (sm != null) {
                spaceComp = sm.groupValues[1].replace("$", "").trim()
                val just = sm.groupValues[2].trim()
                if (just.isNotEmpty()) spaceJust = just
            }
        }

        // Learning Resources
        val resEntry = sections.entries.firstOrNull { it.key.contains("Resource", ignoreCase = true) }
        val resRaw = resEntry?.value.orEmpty()
        val resources = mutableListOf<LearningResource>()
        for (line in resRaw.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("-") || trimmed.startsWith("*") || trimmed.startsWith("•")) {
                val clean = trimmed.removePrefix("-").removePrefix("*").removePrefix("•")
                    .replace("**", "").replace("[", "").replace("]", "").trim()
                val colonIdx = clean.indexOf(':')
                if (colonIdx != -1) {
                    resources.add(
                        LearningResource(
                            topic = clean.substring(0, colonIdx).trim(),
                            description = clean.substring(colonIdx + 1).trim()
                        )
                    )
                } else if (clean.isNotEmpty()) {
                    resources.add(LearningResource("Recommended Practice", clean))
                }
            }
        }
        if (resources.isEmpty()) {
            resources.add(LearningResource("${language.displayName} Practice", "Explore algorithmic problems and syntax idiomatics."))
        }

        return ExplainResult(
            overview = cleanOverview,
            difficulty = diffLevel,
            walkthrough = walkthrough,
            keyConcepts = concepts,
            timeComplexity = timeComp,
            timeJustification = timeJust,
            spaceComplexity = spaceComp,
            spaceJustification = spaceJust,
            learningResources = resources
        )
    }

    private fun parseDebugMarkdown(markdown: String, language: CodeLanguage): DebugResult {
        val sections = splitMarkdownSections(markdown)
        val bugsEntry = sections.entries.firstOrNull { it.key.contains("Bug", ignoreCase = true) }
        val bugsRaw = bugsEntry?.value.orEmpty()
        val bugs = mutableListOf<BugItem>()

        for (line in bugsRaw.lines()) {
            val trimmed = line.trim()
            if (trimmed.matches(Regex("^\\d+\\..*")) || trimmed.startsWith("-") || trimmed.startsWith("*")) {
                val sev = if (trimmed.contains("CRIT", ignoreCase = true) || trimmed.contains("🔴")) {
                    BugSeverity.CRITICAL
                } else {
                    BugSeverity.WARNING
                }
                val lineMatch = Regex("(?:Line|line)\\s*(\\d+)", RegexOption.IGNORE_CASE).find(trimmed)
                val lineNum = lineMatch?.groupValues?.get(1) ?: ""

                val clean = trimmed
                    .replace(Regex("^\\d+\\.\\s*"), "")
                    .replace(Regex("[🔴🟡]"), "")
                    .replace(Regex("\\*\\*(?:CRITICAL|WARNING)\\*\\*"), "")
                    .replace(Regex("\\((?:Line|line)\\s*\\d+\\):?"), "")
                    .trim()

                val parts = clean.split(". ")
                val what = parts.firstOrNull()?.trim() ?: clean
                val why = if (parts.size > 1) parts.drop(1).joinToString(". ").trim() else "May cause runtime errors or faulty calculations."

                bugs.add(BugItem(severity = sev, lineNumber = lineNum, whatIsWrong = what, whyItFails = why))
            }
        }

        // Fixed Code
        val fixedEntry = sections.entries.firstOrNull { it.key.contains("Fixed", ignoreCase = true) }
        val fixedRaw = fixedEntry?.value ?: markdown
        val codeBlockRegex = Regex("```(?:[a-zA-Z0-9_-]+)?\\s*([\\s\\S]*?)```")
        val fixedMatch = codeBlockRegex.find(fixedRaw)
        val fixedCode = fixedMatch?.groupValues?.get(1)?.trim() ?: fixedRaw.trim()

        // Edge Cases
        val edgeEntry = sections.entries.firstOrNull { it.key.contains("Edge", ignoreCase = true) }
        val edgeRaw = edgeEntry?.value.orEmpty()
        val edgeCases = edgeRaw.lines()
            .filter { it.trim().startsWith("-") || it.trim().startsWith("*") || it.trim().startsWith("•") }
            .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
            .filter { it.isNotEmpty() }

        // Best Practices
        val bpEntry = sections.entries.firstOrNull { it.key.contains("Practice", ignoreCase = true) }
        val bpRaw = bpEntry?.value.orEmpty()
        val bestPractices = bpRaw.lines()
            .filter { it.trim().startsWith("-") || it.trim().startsWith("*") || it.trim().startsWith("•") }
            .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
            .filter { it.isNotEmpty() }

        return DebugResult(
            bugs = bugs,
            fixedCode = fixedCode,
            edgeCases = edgeCases.ifEmpty { listOf("Boundary or empty values", "Negative/invalid parameter types") },
            bestPractices = bestPractices.ifEmpty { listOf("Follow idiomatic error checks and avoid unhandled edge cases.") }
        )
    }

    private fun parseOptimizeMarkdown(markdown: String, language: CodeLanguage): OptimizeResult {
        val sections = splitMarkdownSections(markdown)
        val bEntry = sections.entries.firstOrNull { it.key.contains("Bottleneck", ignoreCase = true) }
        val bRaw = bEntry?.value.orEmpty()

        val compMatch = Regex("(?:Current Complexity|Big-?O):?\\s*[`$]?([A-Za-z0-9^()O+*\\s]+)[`$]?", RegexOption.IGNORE_CASE).find(bRaw)
        val currentComp = compMatch?.groupValues?.get(1)?.trim() ?: "O(N^2)"
        val bottlenecks = bRaw.trim().ifEmpty { "Algorithmic bottleneck identified in repetitive iterations." }

        // Optimized Code
        val optEntry = sections.entries.firstOrNull { it.key.contains("Optimized", ignoreCase = true) }
        val optRaw = optEntry?.value ?: markdown
        val codeBlockRegex = Regex("```(?:[a-zA-Z0-9_-]+)?\\s*([\\s\\S]*?)```")
        val optMatch = codeBlockRegex.find(optRaw)
        val optimizedCode = optMatch?.groupValues?.get(1)?.trim() ?: optRaw.trim()

        // Comparison Table
        val compTableEntry = sections.entries.firstOrNull { it.key.contains("Comparison", ignoreCase = true) || it.key.contains("Before", ignoreCase = true) }
        val compTableRaw = compTableEntry?.value.orEmpty()
        val rows = mutableListOf<ComparisonRow>()

        for (line in compTableRaw.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("|") && !trimmed.contains("---") && !trimmed.contains("METRIC", ignoreCase = true) && !trimmed.contains("Metric")) {
                val parts = trimmed.split("|").map { it.trim() }.filter { it.isNotEmpty() }
                if (parts.size >= 3) {
                    rows.add(
                        ComparisonRow(
                            metric = parts[0].replace("**", ""),
                            before = parts[1].replace("`", ""),
                            after = parts[2].replace("`", "")
                        )
                    )
                }
            }
        }
        if (rows.isEmpty()) {
            rows.add(ComparisonRow("Time Complexity", currentComp, "O(N)"))
            rows.add(ComparisonRow("Space Complexity", "O(N)", "O(1)"))
            rows.add(ComparisonRow("Readability Score", "6/10", "9/10"))
        }

        // Alternative Approach
        val altEntry = sections.entries.firstOrNull { it.key.contains("Alternative", ignoreCase = true) }
        val altRaw = altEntry?.value.orEmpty()
        val altApproach = if (altRaw.isNotBlank()) {
            val altSnippetMatch = codeBlockRegex.find(altRaw)
            val snippet = altSnippetMatch?.groupValues?.get(1)?.trim().orEmpty()
            val textWithoutSnippet = altRaw.replace(Regex("```[\\s\\S]*?```"), "").trim()
            val titleMatch = Regex("\\*\\*([^*]+)\\*\\*").find(textWithoutSnippet)
            val title = titleMatch?.groupValues?.get(1) ?: "Alternative Technique"
            val exp = textWithoutSnippet.replace(Regex("\\*\\*[^*]+\\*\\*:?"), "").trim()
            AlternativeApproach(
                title = title,
                explanation = exp.ifEmpty { "Alternative algorithmic implementation." },
                codeSnippet = snippet
            )
        } else null

        return OptimizeResult(
            bottlenecks = bottlenecks,
            currentComplexity = currentComp,
            optimizedCode = optimizedCode,
            comparisonTable = rows,
            alternativeApproach = altApproach
        )
    }
}
