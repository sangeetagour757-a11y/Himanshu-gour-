package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(text: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        val lines = text.split("\n")
        var inCodeBlock = false
        var codeBlockText = ""

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("```")) {
                if (inCodeBlock) {
                    // Render code block
                    CodeBlock(codeBlockText.trim())
                    codeBlockText = ""
                    inCodeBlock = false
                } else {
                    inCodeBlock = true
                }
                continue
            }

            if (inCodeBlock) {
                codeBlockText += line + "\n"
                continue
            }

            when {
                trimmed.startsWith("# ") -> {
                    Text(
                        text = trimmed.substring(2),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("## ") -> {
                    Text(
                        text = trimmed.substring(3),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                }
                trimmed.startsWith("### ") || trimmed.startsWith("#### ") -> {
                    val actualText = trimmed.substring(trimmed.indexOf(" ") + 1)
                    Text(
                        text = actualText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                    )
                }
                trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = parseInlineMarkdown(trimmed.substring(2)),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp
                        )
                    }
                }
                trimmed.isNotEmpty() && trimmed.indexOf(". ") in 1..4 && trimmed.substring(0, trimmed.indexOf(". ")).all { it.isDigit() } -> {
                    // Numbered list
                    val index = trimmed.indexOf(". ")
                    val num = trimmed.substring(0, index + 1)
                    val contentText = trimmed.substring(index + 2)
                    Row(modifier = Modifier.padding(start = 8.dp, top = 2.dp)) {
                        Text(
                            text = "$num ",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = parseInlineMarkdown(contentText),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp
                        )
                    }
                }
                else -> {
                    if (trimmed.isNotEmpty()) {
                        Text(
                            text = parseInlineMarkdown(line),
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
        if (inCodeBlock && codeBlockText.isNotEmpty()) {
            CodeBlock(codeBlockText.trim())
        }
    }
}

@Composable
fun CodeBlock(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp)
    ) {
        Text(
            text = code,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun parseInlineMarkdown(text: String): androidx.compose.ui.text.AnnotatedString {
    return androidx.compose.ui.text.buildAnnotatedString {
        var cursor = 0
        while (cursor < text.length) {
            val boldStart = text.indexOf("**", cursor)
            if (boldStart != -1) {
                val boldEnd = text.indexOf("**", boldStart + 2)
                if (boldEnd != -1) {
                    append(text.substring(cursor, boldStart))
                    pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                    append(text.substring(boldStart + 2, boldEnd))
                    pop()
                    cursor = boldEnd + 2
                    continue
                }
            }
            append(text.substring(cursor))
            break
        }
    }
}
