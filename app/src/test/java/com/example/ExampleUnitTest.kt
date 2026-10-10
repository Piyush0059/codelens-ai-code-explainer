package com.example

import com.example.ui.components.MarkdownBlock
import com.example.ui.components.parseMarkdownBlocks
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testParseMarkdownBlocks_emptyAndWhitespace() {
        assertTrue(parseMarkdownBlocks("").isEmpty())
        assertTrue(parseMarkdownBlocks("   \n\n\t  ").isEmpty())
    }

    @Test
    fun testParseMarkdownBlocks_isolatedHash_noInfiniteLoop() {
        // This was the exact bug that caused OutOfMemoryError
        val blocks1 = parseMarkdownBlocks("##")
        assertEquals(1, blocks1.size)
        assertTrue(blocks1[0] is MarkdownBlock.Header)

        val blocks2 = parseMarkdownBlocks("###")
        assertEquals(1, blocks2.size)

        val blocks3 = parseMarkdownBlocks("#")
        assertEquals(1, blocks3.size)
    }

    @Test
    fun testParseMarkdownBlocks_codeBlocks() {
        // Unclosed code block during streaming
        val unclosed = parseMarkdownBlocks("```python\ndef hello():\n    print('world')")
        assertEquals(1, unclosed.size)
        val codeBlock = unclosed[0] as MarkdownBlock.Code
        assertEquals("python", codeBlock.language)
        assertTrue(codeBlock.code.contains("print('world')"))

        // Closed code block
        val closed = parseMarkdownBlocks("```java\nint x = 42;\n```")
        assertEquals(1, closed.size)
    }

    @Test
    fun testParseMarkdownBlocks_headersAndBulletsAndTables() {
        val markdown = """
            ## 1. Overview
            This is a summary paragraph.
            
            ### 2. Walkthrough
            - Step 1: Initialize variables
            * Step 2: Loop through items
            1. Step 3: Return result
            
            | Metric | Before | After |
            | --- | --- | --- |
            | Time | O(N^2) | O(N) |
        """.trimIndent()

        val blocks = parseMarkdownBlocks(markdown)
        assertTrue(blocks.isNotEmpty())
        assertTrue(blocks.any { it is MarkdownBlock.Header })
        assertTrue(blocks.any { it is MarkdownBlock.Paragraph })
        assertTrue(blocks.any { it is MarkdownBlock.Bullet })
        assertTrue(blocks.any { it is MarkdownBlock.Table })
    }

    @Test
    fun testParseMarkdownBlocks_streamingPrefixesSimulation() {
        val fullMarkdown = """
            ## 1. Overview
            [Difficulty: BEGINNER]
            A function that reverses an array in place.
            
            ## 2. Line-by-Line Walkthrough
            - **Lines 1-3**: Two-pointer swap approach.
            - **Lines 4-5**: Return result array.
            
            ```python
            def reverse(arr):
                return arr[::-1]
            ```
            
            | Metric | Before | After |
            | Time | O(N) | O(N) |
        """.trimIndent()

        // Test every single prefix of fullMarkdown from length 1 to end
        // If there were any infinite loop or OOM bug, this would fail or hang
        for (i in 1..fullMarkdown.length) {
            val prefix = fullMarkdown.substring(0, i)
            val blocks = parseMarkdownBlocks(prefix)
            assertNotNull(blocks)
        }
    }
}
