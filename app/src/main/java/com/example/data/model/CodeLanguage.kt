package com.example.data.model

enum class CodeLanguage(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val fileExtension: String,
    val sampleCode: String,
    val sampleDescription: String
) {
    PYTHON(
        id = "python",
        displayName = "Python",
        iconEmoji = "🐍",
        fileExtension = "py",
        sampleDescription = "Buggy list statistics with off-by-one & zero-division issues",
        sampleCode = """def find_average_and_max(items=[]):
    # Calculate average and maximum from list
    total = 0
    max_val = 0
    for i in range(len(items) + 1):  # Bug: off-by-one index error
        val = items[i]
        total += val
        if val > max_val:
            max_val = val
    avg = total / len(items)  # Bug: ZeroDivisionError if empty
    return {"average": avg, "max": max_val}

# Sample call
result = find_average_and_max([14, 28, 42, 7])
print(result)"""
    ),
    JAVA(
        id = "java",
        displayName = "Java",
        iconEmoji = "☕",
        fileExtension = "java",
        sampleDescription = "Array search with null pointer and boundary flaw",
        sampleCode = """public class NumberSearch {
    public static int findTarget(int[] nums, int target) {
        // Bug: missing null or empty check
        for (int i = 0; i <= nums.length; i++) { // Bug: ArrayIndexOutOfBounds
            if (nums[i] == target) {
                return i;
            }
        }
        return -1;
    }
}"""
    ),
    CPP(
        id = "cpp",
        displayName = "C++",
        iconEmoji = "⚡",
        fileExtension = "cpp",
        sampleDescription = "Vector duplicate check with quadratic complexity & iterator bug",
        sampleCode = """#include <vector>
#include <iostream>

bool hasDuplicates(const std::vector<int>& arr) {
    // Inefficient O(N^2) comparison with signed/unsigned warning
    for (size_t i = 0; i < arr.size(); ++i) {
        for (size_t j = i; j < arr.size(); ++j) { // Bug: j=i checks self!
            if (arr[i] == arr[j]) {
                return true;
            }
        }
    }
    return false;
}"""
    ),
    JAVASCRIPT(
        id = "javascript",
        displayName = "JavaScript",
        iconEmoji = "🟨",
        fileExtension = "js",
        sampleDescription = "Asynchronous fetch with unhandled promise & closure bug",
        sampleCode = """async function fetchUserData(userIds) {
    const results = [];
    for (var i = 0; i <= userIds.length; i++) {
        // Bug: out-of-bounds index & var closure scope
        setTimeout(() => {
            console.log("Processing index: " + i);
        }, 100);
        const res = fetch('/api/user/' + userIds[i]);
        // Bug: missing await on fetch response
        results.push(res.json());
    }
    return results;
}"""
    ),
    C(
        id = "c",
        displayName = "C",
        iconEmoji = "🔷",
        fileExtension = "c",
        sampleDescription = "String reverser with buffer overflow and memory leak",
        sampleCode = """#include <stdio.h>
#include <stdlib.h>
#include <string.h>

char* reverseString(char* str) {
    int len = strlen(str);
    char* copy = (char*)malloc(len); // Bug: missing +1 for null terminator
    for (int i = 0; i < len; i++) {
        copy[i] = str[len - i]; // Bug: off-by-one indexing
    }
    return copy; // Bug: caller might leak memory
}"""
    ),
    GO(
        id = "go",
        displayName = "Go",
        iconEmoji = "🦫",
        fileExtension = "go",
        sampleDescription = "Goroutine loop variable capture & missing wait group",
        sampleCode = """package main

import "fmt"

func processItems(items []string) {
    for _, item := range items {
        // Bug: goroutine captures loop variable pointer in older Go
        go func() {
            fmt.Println("Processing:", item)
        }()
    }
    // Bug: main returns immediately before goroutines finish
}"""
    ),
    RUST(
        id = "rust",
        displayName = "Rust",
        iconEmoji = "🦀",
        fileExtension = "rs",
        sampleDescription = "Ownership conflict and unhandled Option unwrap",
        sampleCode = """fn get_first_element(v: Vec<i32>) -> i32 {
    let first = v.get(0).unwrap(); // Bug: panic if vector is empty
    println!("Vector length: {}", v.len());
    *first
}"""
    );

    companion object {
        fun fromId(id: String): CodeLanguage {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: PYTHON
        }
    }
}
