package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.CalculatorViewModel
import com.example.ui.components.MarkdownText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SolverScreen(viewModel: CalculatorViewModel) {
    val problemInput by viewModel.solverProblemInput.collectAsStateWithLifecycle()
    val explanation by viewModel.solverExplanation.collectAsStateWithLifecycle()
    val isLoading by viewModel.solverLoading.collectAsStateWithLifecycle()

    val templateQuestions = listOf(
        "Solve for x: 3x^2 - 12x + 9 = 0",
        "Derivate of ln(x)/x with respect to x",
        "Find the limit: x->0 sin(x)/x",
        "Explain equation: E = mc^2",
        "Derivation of the Fibonacci closed-form",
        "Why is division by zero undefined?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "AI Mathematical Science Solver",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Type any word problem, system of equations, calculus request, or scientific formula derivation. Our AI will compute and walk you through every step.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )

                // SUGGESTIONS LazyRow
                Text(
                    text = "Suggested Questions:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(templateQuestions) { prompt ->
                        SuggestionChip(
                            onClick = { viewModel.updateProblemInput(prompt) },
                            label = { Text(prompt, maxLines = 1, fontSize = 12.sp) },
                            modifier = Modifier.testTag("suggest_$prompt")
                        )
                    }
                }

                // INPUT AREA
                OutlinedTextField(
                    value = problemInput,
                    onValueChange = { viewModel.updateProblemInput(it) },
                    placeholder = { Text("E.g., Find the integral of x*e^-x from 0 to infinity...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("solver_input"),
                    shape = RoundedCornerShape(16.dp),
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Clear Button
                    if (problemInput.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.clearSolver() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset", color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    // Solve Button
                    Button(
                        onClick = { viewModel.solveProblem() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("solve_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && problemInput.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deriving Steps...")
                        } else {
                            Icon(Icons.Default.Send, contentDescription = "Solve")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Solve & Explain Steps", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // RESULTS SCROLL BOX
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (explanation != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = "Derivation Output:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        MarkdownText(
                            text = explanation ?: "",
                            modifier = Modifier.fillMaxWidth().testTag("solver_output_text")
                        )
                    }
                } else if (isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Consulting AI Scholar...",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Calculating intermediate formulas",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No problems solved yet",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Submit a prompt or pick a suggestion above",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        )
                    }
                }
            }
        }
    }
}
