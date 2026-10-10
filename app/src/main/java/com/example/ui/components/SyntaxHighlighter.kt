package com.example.ui.components

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.data.model.CodeLanguage
import com.example.ui.theme.SyntaxBuiltin
import com.example.ui.theme.SyntaxComment
import com.example.ui.theme.SyntaxKeyword
import com.example.ui.theme.SyntaxNumber
import com.example.ui.theme.SyntaxOperator
import com.example.ui.theme.SyntaxString
import com.example.ui.theme.SyntaxType
import com.example.ui.theme.TextPrimary

object SyntaxHighlighter {

    private val KEYWORDS = setOf(
        "def", "class", "return", "if", "else", "elif", "for", "while", "in", "import", "from",
        "as", "try", "except", "finally", "with", "async", "await", "lambda", "yield", "pass",
        "function", "const", "let", "var", "new", "this", "typeof", "instanceof", "throw", "catch",
        "public", "private", "protected", "static", "final", "void", "int", "boolean", "char",
        "float", "double", "long", "short", "byte", "extends", "implements", "interface", "package",
        "struct", "enum", "typedef", "sizeof", "include", "template", "typename", "namespace",
        "fn", "mut", "impl", "trait", "pub", "use", "mod", "match", "loop", "where", "ref", "self",
        "func", "go", "select", "chan", "defer", "map", "range", "type", "switch", "case", "default"
    )

    private val BUILTINS_AND_TYPES = setOf(
        "True", "False", "None", "true", "false", "null", "nil", "undefined",
        "print", "len", "range", "enumerate", "zip", "sum", "max", "min", "list", "dict", "set",
        "String", "Integer", "Boolean", "List", "Map", "Set", "ArrayList", "HashMap",
        "vector", "string", "cout", "cin", "endl", "printf", "scanf", "malloc", "free", "strlen",
        "fmt", "Println", "Printf", "Sprintf", "make", "append", "panic", "recover",
        "Vec", "Option", "Result", "Some", "Ok", "Err", "println!", "format!", "vec!"
    )

    fun highlight(code: String, language: CodeLanguage): AnnotatedString {
        if (code.isEmpty()) return AnnotatedString("")

        return buildAnnotatedString {
            append(code)

            var i = 0
            val len = code.length

            while (i < len) {
                val c = code[i]

                // Line comment check
                val isPythonComment = (language == CodeLanguage.PYTHON) && c == '#'
                val isSlashComment = (c == '/' && i + 1 < len && code[i + 1] == '/')

                if (isPythonComment || isSlashComment) {
                    val start = i
                    val end = code.indexOf('\n', start).let { if (it == -1) len else it }
                    addStyle(
                        SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic),
                        start,
                        end
                    )
                    i = end
                    continue
                }

                // Block comment /* ... */
                if (c == '/' && i + 1 < len && code[i + 1] == '*') {
                    val start = i
                    val end = code.indexOf("*/", start + 2).let { if (it == -1) len else it + 2 }
                    addStyle(
                        SpanStyle(color = SyntaxComment, fontStyle = FontStyle.Italic),
                        start,
                        end
                    )
                    i = end
                    continue
                }

                // Triple quotes in Python
                if (c == '"' && i + 2 < len && code[i + 1] == '"' && code[i + 2] == '"') {
                    val start = i
                    val end = code.indexOf("\"\"\"", start + 3).let { if (it == -1) len else it + 3 }
                    addStyle(SpanStyle(color = SyntaxString), start, end)
                    i = end
                    continue
                }

                // String literals "..." or '...' or `...`
                if (c == '"' || c == '\'' || c == '`') {
                    val quote = c
                    val start = i
                    var end = i + 1
                    var escaped = false
                    while (end < len) {
                        val current = code[end]
                        if (escaped) {
                            escaped = false
                        } else if (current == '\\') {
                            escaped = true
                        } else if (current == quote) {
                            end++
                            break
                        } else if (current == '\n' && quote != '`') {
                            break
                        }
                        end++
                    }
                    addStyle(SpanStyle(color = SyntaxString), start, end)
                    i = end
                    continue
                }

                // Numbers
                if (c.isDigit() || (c == '.' && i + 1 < len && code[i + 1].isDigit())) {
                    val start = i
                    var end = i
                    while (end < len && (code[end].isLetterOrDigit() || code[end] == '.' || code[end] == '_')) {
                        end++
                    }
                    addStyle(SpanStyle(color = SyntaxNumber), start, end)
                    i = end
                    continue
                }

                // Identifiers & Keywords
                if (c.isLetter() || c == '_' || c == '$') {
                    val start = i
                    var end = i
                    while (end < len && (code[end].isLetterOrDigit() || code[end] == '_' || code[end] == '$' || code[end] == '!')) {
                        end++
                    }
                    val word = code.substring(start, end)
                    when {
                        KEYWORDS.contains(word) -> {
                            addStyle(
                                SpanStyle(color = SyntaxKeyword, fontWeight = FontWeight.Bold),
                                start,
                                end
                            )
                        }
                        BUILTINS_AND_TYPES.contains(word) -> {
                            addStyle(SpanStyle(color = SyntaxBuiltin), start, end)
                        }
                        word.startsWith("@") -> {
                            addStyle(SpanStyle(color = SyntaxType), start, end)
                        }
                        end < len && code[end] == '(' -> {
                            // Function call
                            addStyle(SpanStyle(color = SyntaxType), start, end)
                        }
                    }
                    i = end
                    continue
                }

                // Operators & Braces
                if ("{}[]()<>=+-*/%!&|^~:;,".contains(c)) {
                    addStyle(SpanStyle(color = SyntaxOperator), i, i + 1)
                }

                i++
            }
        }
    }
}
